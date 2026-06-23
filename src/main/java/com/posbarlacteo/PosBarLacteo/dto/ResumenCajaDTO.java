package com.posbarlacteo.PosBarLacteo.dto;

public class ResumenCajaDTO {
    private Double fondoInicial;
    private Double ventasEfectivo;
    private Double ventasTarjeta;
    private Double ingresosExtra;
    private Double retiros;
    private Double totalEnCaja;

    public ResumenCajaDTO() {}

    public ResumenCajaDTO(Double fondoInicial, Double ventasEfectivo, Double ventasTarjeta, 
                          Double ingresosExtra, Double retiros, Double totalEnCaja) {
        this.fondoInicial = fondoInicial;
        this.ventasEfectivo = ventasEfectivo;
        this.ventasTarjeta = ventasTarjeta;
        this.ingresosExtra = ingresosExtra;
        this.retiros = retiros;
        this.totalEnCaja = totalEnCaja;
    }

    // Getters y Setters
    public Double getFondoInicial() { return fondoInicial; }
    public void setFondoInicial(Double fondoInicial) { this.fondoInicial = fondoInicial; }

    public Double getVentasEfectivo() { return ventasEfectivo; }
    public void setVentasEfectivo(Double ventasEfectivo) { this.ventasEfectivo = ventasEfectivo; }

    public Double getVentasTarjeta() { return ventasTarjeta; }
    public void setVentasTarjeta(Double ventasTarjeta) { this.ventasTarjeta = ventasTarjeta; }

    public Double getIngresosExtra() { return ingresosExtra; }
    public void setIngresosExtra(Double ingresosExtra) { this.ingresosExtra = ingresosExtra; }

    public Double getRetiros() { return retiros; }
    public void setRetiros(Double retiros) { this.retiros = retiros; }

    public Double getTotalEnCaja() { return totalEnCaja; }
    public void setTotalEnCaja(Double totalEnCaja) { this.totalEnCaja = totalEnCaja; }
}