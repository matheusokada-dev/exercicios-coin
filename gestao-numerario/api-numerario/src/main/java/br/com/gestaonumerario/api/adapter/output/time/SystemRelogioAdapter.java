package br.com.gestaonumerario.api.adapter.output.time;

import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
public class SystemRelogioAdapter implements RelogioOutputPort {

    @Override
    public Instant agora() {
        return Instant.now();
    }

    @Override
    public LocalDate hoje() {
        return LocalDate.now(ZoneOffset.UTC);
    }
}


