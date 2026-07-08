package com.posbarlacteo.PosBarLacteo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rut_empresa", nullable = false, unique = true, length = 12)
    private String rutEmpresa;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private String giro;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String comuna;

    @Column(name = "haulmer_api_key", length = 500)
    private String haulmerApiKey;

    @Column(name = "getnet_client_id")
    private String getnetClientId;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", updatable = false, insertable = false)
    private LocalDateTime fechaRegistro;

    // ─── CONSTRUCTORES ───
    public Empresa() {
    }

    public Empresa(Long id) {
        this.id = id;
    }

    // ─── GETTERS Y SETTERS ───
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRutEmpresa() {
        return rutEmpresa;
    }

    public void setRutEmpresa(String rutEmpresa) {
        this.rutEmpresa = rutEmpresa;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getGiro() {
        return giro;
    }

    public void setGiro(String giro) {
        this.giro = giro;
    }

    public String getDireccion() {
        return direccion;
    }

    // ✨ CORREGIDO: Se cerró el paréntesis correctamente y se agregó el cuerpo del método
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getHaulmerApiKey() {
        return haulmerApiKey;
    }

    public void setHaulmerApiKey(String haulmerApiKey) {
        this.haulmerApiKey = haulmerApiKey;
    }

    public String getGetnetClientId() {
        return getnetClientId;
    }

    public void setGetnetClientId(String getnetClientId) {
        this.getnetClientId = getnetClientId;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}