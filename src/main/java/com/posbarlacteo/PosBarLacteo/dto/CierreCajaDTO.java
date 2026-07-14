package com.posbarlacteo.PosBarLacteo.dto;

import java.math.BigDecimal;

public class CierreCajaDTO {
    private BigDecimal fondoInicial;
    private BigDecimal ventasEfectivo;
    private BigDecimal ventasTarjeta;
    private BigDecimal ingresosExtra;
    private BigDecimal retiros;
    private BigDecimal totalSistema;
    private BigDecimal totalRealFisico;
    private BigDecimal diferencia;

    public CierreCajaDTO() {}

    public BigDecimal getFondoInicial() { return fondoInicial; }
    public void setFondoInicial(BigDecimal fondoInicial) { this.fondoInicial = fondoInicial; }

    public BigDecimal getVentasEfectivo() { return ventasEfectivo; }
    public void setVentasEfectivo(BigDecimal ventasEfectivo) { this.ventasEfectivo = ventasEfectivo; }

    public BigDecimal getVentasTarjeta() { return ventasTarjeta; }
    public void setVentasTarjeta(BigDecimal ventasTarjeta) { this.ventasTarjeta = ventasTarjeta; }

    public BigDecimal getIngresosExtra() { return ingresosExtra; }
    public void setIngresosExtra(BigDecimal ingresosExtra) { this.ingresosExtra = ingresosExtra; }

    public BigDecimal getRetiros() { return retiros; }
    public void setRetiros(BigDecimal retiros) { this.retiros = retiros; }

    public BigDecimal getTotalSistema() { return totalSistema; }
    public void setTotalSistema(BigDecimal totalSistema) { this.totalSistema = totalSistema; }

    public BigDecimal getTotalRealFisico() { return totalRealFisico; }
    public void setTotalRealFisico(BigDecimal totalRealFisico) { this.totalRealFisico = totalRealFisico; }

    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }
}