package com.posbarlacteo.PosBarLacteo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "turnos_caja")
public class TurnoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cajero_id", nullable = false)
    private Long cajeroId;

    @Column(name = "monto_apertura", nullable = false)
    private BigDecimal montoApertura;

    @Column(nullable = false)
    private String estado; 

    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    // --- Constructor vacío requerido por JPA ---
    public TurnoCaja() {
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCajeroId() {
        return cajeroId;
    }

    public void setCajeroId(Long cajeroId) {
        this.cajeroId = cajeroId;
    }

    public BigDecimal getMontoApertura() {
        return montoApertura;
    }

    public void setMontoApertura(BigDecimal montoApertura) {
        this.montoApertura = montoApertura;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }
}