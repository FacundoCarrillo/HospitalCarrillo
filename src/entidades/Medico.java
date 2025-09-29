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
public class Medico extends Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Se guarda solo el número para que el builder sea sencillo */
    private final String numeroMatricula;

    private final EspecialidadMedica especialidad;

    @Setter
    private Departamento departamento;

    @lombok.Builder.Default
    private final List<Cita> citas = new ArrayList<>();

    /** Getter que devuelve el objeto Matricula a partir del número */
    public Matricula getMatricula() {
        return new Matricula(
                Objects.requireNonNull(numeroMatricula, "El número de matrícula no puede ser nulo"));
    }

    public void addCita(Cita cita) {
        if (cita != null) {
            this.citas.add(cita);
        }
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(citas);
    }
}


