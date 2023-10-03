package org.jobrunr.quarkus.autoconfigure.health;

import jakarta.enterprise.inject.Instance;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.jobrunr.quarkus.autoconfigure.JobRunrBuildTimeConfiguration;
import org.jobrunr.quarkus.autoconfigure.JobRunrRuntimeConfiguration;
import org.jobrunr.server.BackgroundJobServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobRunrHealthCheckTest {

    private JobRunrBuildTimeConfiguration.BackgroundJobServerConfiguration backgroundJobServerConfiguration;

    @Mock
    private Instance<BackgroundJobServer> backgroundJobServerProviderInstance;

    @Mock
    private BackgroundJobServer backgroundJobServer;

    private JobRunrHealthCheck jobRunrHealthCheck;

    @BeforeEach
    void setUpHealthIndicator() {
        final JobRunrBuildTimeConfiguration jobRunrRuntimeConfiguration = new JobRunrBuildTimeConfiguration();
        backgroundJobServerConfiguration = new JobRunrBuildTimeConfiguration.BackgroundJobServerConfiguration();
        jobRunrRuntimeConfiguration.backgroundJobServer = backgroundJobServerConfiguration;

        lenient().when(backgroundJobServerProviderInstance.get()).thenReturn(backgroundJobServer);

        jobRunrHealthCheck = new JobRunrHealthCheck(jobRunrRuntimeConfiguration, backgroundJobServerProviderInstance);
    }

    @Test
    void givenDisabledBackgroundJobServer_ThenHealthIsOutOfService() {
        backgroundJobServerConfiguration.enabled = false;
        assertThat(jobRunrHealthCheck.call().getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
    }

    @Test
    void givenEnabledBackgroundJobServerAndBackgroundJobServerRunning_ThenHealthIsUp() {
        backgroundJobServerConfiguration.enabled = true;
        when(backgroundJobServer.isRunning()).thenReturn(true);

        assertThat(jobRunrHealthCheck.call().getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
    }

    @Test
    void givenEnabledBackgroundJobServerAndBackgroundJobServerStopped_ThenHealthIsDown() {
        backgroundJobServerConfiguration.enabled = true;
        when(backgroundJobServer.isRunning()).thenReturn(false);

        assertThat(jobRunrHealthCheck.call().getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
    }
}