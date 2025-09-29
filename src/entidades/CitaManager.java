package entidades;

import lombok.Data;

import java.io.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class CitaManager implements CitaService {

    private final List<Cita> citas = new ArrayList<>();
    private final Map<Paciente, List<Cita>> citasPorPaciente = new ConcurrentHashMap<>();
    private final Map<Medico, List<Cita>> citasPorMedico = new ConcurrentHashMap<>();
    private final Map<Sala, List<Cita>> citasPorSala = new ConcurrentHashMap<>();

    @Override
    public Cita programarCita(Paciente paciente, Medico medico, Sala sala,
                              LocalDateTime fechaHora, BigDecimal costo) throws CitaException {

        validarCita(fechaHora, costo);

        if (!esMedicoDisponible(medico, fechaHora)) {
            throw new CitaException("El médico no está disponible en la fecha y hora solicitadas.");
        }

        if (!esSalaDisponible(sala, fechaHora)) {
            throw new CitaException("La sala no está disponible en la fecha y hora solicitadas.");
        }

        if (!medico.getEspecialidad().equals(sala.getDepartamento().getEspecialidad())) {
            throw new CitaException("La especialidad del médico no coincide con el departamento de la sala.");
        }

        Cita cita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .sala(sala)
                .fechaHora(fechaHora)
                .costo(costo)
                .build();

        citas.add(cita);
        actualizarIndicePaciente(paciente, cita);
        actualizarIndiceMedico(medico, cita);
        actualizarIndiceSala(sala, cita);

        paciente.addCita(cita);
        medico.addCita(cita);
        sala.addCita(cita);

        return cita;
    }

    private void validarCita(LocalDateTime fechaHora, BigDecimal costo) throws CitaException {
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new CitaException("No se puede programar una cita en el pasado.");
        }

        if (costo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CitaException("El costo debe ser mayor que cero.");
        }
    }

    private boolean esMedicoDisponible(Medico medico, LocalDateTime fechaHora) {
        List<Cita> citasExistentes = citasPorMedico.get(medico);
        if (citasExistentes != null) {
            for (Cita citaExistente : citasExistentes) {
                long horas = Duration.between(citaExistente.getFechaHora(), fechaHora).toHours();
                if (Math.abs(horas) < 2) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean esSalaDisponible(Sala sala, LocalDateTime fechaHora) {
        List<Cita> citasExistentes = citasPorSala.get(sala);
        if (citasExistentes != null) {
            for (Cita citaExistente : citasExistentes) {
                long horas = Duration.between(citaExistente.getFechaHora(), fechaHora).toHours();
                if (Math.abs(horas) < 2) {
                    return false;
                }
            }
        }
        return true;
    }

    private void actualizarIndicePaciente(Paciente paciente, Cita cita) {
        citasPorPaciente.computeIfAbsent(paciente, p -> new ArrayList<>()).add(cita);
    }

    private void actualizarIndiceMedico(Medico medico, Cita cita) {
        citasPorMedico.computeIfAbsent(medico, m -> new ArrayList<>()).add(cita);
    }

    private void actualizarIndiceSala(Sala sala, Cita cita) {
        citasPorSala.computeIfAbsent(sala, s -> new ArrayList<>()).add(cita);
    }

    @Override
    public List<Cita> getCitasPorPaciente(Paciente paciente) {
        return citasPorPaciente.containsKey(paciente)
                ? Collections.unmodifiableList(citasPorPaciente.get(paciente))
                : Collections.emptyList();
    }

    @Override
    public List<Cita> getCitasPorMedico(Medico medico) {
        return citasPorMedico.containsKey(medico)
                ? Collections.unmodifiableList(citasPorMedico.get(medico))
                : Collections.emptyList();
    }

    @Override
    public List<Cita> getCitasPorSala(Sala sala) {
        return citasPorSala.containsKey(sala)
                ? Collections.unmodifiableList(citasPorSala.get(sala))
                : Collections.emptyList();
    }

    @Override
    public void guardarCitas(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Cita cita : citas) {
                writer.println(cita.toCsvString());
            }
        }
    }

    @Override
    public void cargarCitas(String filename, Map<String, Paciente> pacientes,
                            Map<String, Medico> medicos, Map<String, Sala> salas)
            throws IOException, ClassNotFoundException, CitaException {

        citas.clear();
        citasPorPaciente.clear();
        citasPorMedico.clear();
        citasPorSala.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    Cita cita = Cita.fromCsvString(line, pacientes, medicos, salas);
                    citas.add(cita);
                    actualizarIndicePaciente(cita.getPaciente(), cita);
                    actualizarIndiceMedico(cita.getMedico(), cita);
                    actualizarIndiceSala(cita.getSala(), cita);
                } catch (CitaException e) {
                    System.err.printf("Error al cargar cita [%s]: %s%n", line, e.getMessage());
                    throw e;
                }
            }
        }
    }
}

