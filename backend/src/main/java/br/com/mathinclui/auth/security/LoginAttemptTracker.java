package br.com.mathinclui.auth.security;

import java.time.Clock; import java.time.Duration; import java.time.Instant; import java.util.concurrent.ConcurrentHashMap;
public final class LoginAttemptTracker {
    private final int maxAttempts; private final Duration blockDuration; private final Clock clock; private final ConcurrentHashMap<String,AttemptState> attempts=new ConcurrentHashMap<>();
    public LoginAttemptTracker(int maxAttempts,Duration blockDuration,Clock clock){ if(maxAttempts<1) throw new IllegalArgumentException("maxAttempts must be positive"); if(blockDuration.isNegative()||blockDuration.isZero()) throw new IllegalArgumentException("blockDuration must be positive"); this.maxAttempts=maxAttempts; this.blockDuration=blockDuration; this.clock=clock; }
    public boolean isBlocked(String key){ var state=attempts.get(key); if(state==null||state.blockedUntil()==null)return false; var now=clock.instant(); if(!now.isBefore(state.blockedUntil())){ attempts.remove(key,state); return false;} return true; }
    public long retryAfterSeconds(String key){ var state=attempts.get(key); if(state==null||state.blockedUntil()==null)return 0; return Math.max(0,Duration.between(clock.instant(),state.blockedUntil()).toSeconds()); }
    public void recordFailure(String key){ var now=clock.instant(); attempts.compute(key,(ignored,state)->{ if(state!=null&&state.blockedUntil()!=null&&now.isBefore(state.blockedUntil())) return state; var failures=state==null?1:state.failures()+1; var blockedUntil=failures>=maxAttempts?now.plus(blockDuration):null; return new AttemptState(failures,blockedUntil);}); }
    public void recordSuccess(String key){ attempts.remove(key); }
    private record AttemptState(int failures,Instant blockedUntil){}
}
