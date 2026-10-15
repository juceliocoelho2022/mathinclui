package br.com.mathinclui.system;

public final class SystemInfoProvider {
    private SystemInfoProvider() {}
    public static SystemInfo current() { return new SystemInfo("MathInclui", "0.2.3", "UP"); }
}
