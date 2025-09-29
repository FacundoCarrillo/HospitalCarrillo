package entidades;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@SuperBuilder
public class Paciente extends Persona implements Serializable {
    private final String telefono;
    private final String direccion;
    private final HistoriaClinica historiaClinica;

    private final List<Cita> citas = new ArrayList<>();
    @Setter
    private Hospital hospital;

    protected Paciente(PacienteBuilder<?, ?> builder) {
        super(builder);
        this.telefono = validarString(builder.telefono, "El teléfono no puede ser nulo ni vacío");
        this.direccion = validarString(builder.direccion, "La dirección no puede ser nula ni vacía");
        this.historiaClinica = HistoriaClinica.builder().paciente(this).build();
    }

    public void addCita(Cita cita) {
        if (!citas.contains(cita)) citas.add(cita);
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(new ArrayList<>(citas));
    }

    private String validarString(String valor, String mensajeError) {
        Objects.requireNonNull(valor, mensajeError);
        if (valor.trim().isEmpty()) throw new IllegalArgumentException(mensajeError);
        return valor;
    }

    public static abstract class PacienteBuilder<C extends Paciente, B extends PacienteBuilder<C, B>> extends PersonaBuilder<C, B> {
        private String telefono;
        private String direccion;

        public B telefono(String telefono) { this.telefono = telefono; return self(); }
        public B direccion(String direccion) { this.direccion = direccion; return self(); }
    }
}
