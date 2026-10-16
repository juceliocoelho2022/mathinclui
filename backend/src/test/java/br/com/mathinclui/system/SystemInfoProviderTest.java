package br.com.mathinclui.system;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SystemInfoProviderTest {
    @Test
    void shouldExposeCurrentApplicationIdentity() {
        var info = SystemInfoProvider.current();
        assertThat(info.name()).isEqualTo("MathInclui");
        assertThat(info.version()).isEqualTo("0.4.0");
        assertThat(info.status()).isEqualTo("UP");
    }
}
