

package com.posbarlacteo.PosBarLacteo.model;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
@Entity
@Table(name = "pos_productos")
@Data
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_barras", unique = true)
    private String codigoBarras;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    @Column(name = "es_insumo")
    private boolean esInsumo;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Double stock;

    @Column(name = "stock_critico")
    private Double stockCritico;

    @Column(name = "activo")
    private boolean activo = true;

    @JsonIgnore 
    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @JsonManagedReference
    @OneToMany(mappedBy = "productoPrincipal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Receta> receta;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
}
