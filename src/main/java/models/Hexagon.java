package models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import models.enums.TipusTerreny;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "hexagon")
@Check(constraints = "numero is null or (numero between 2 and 12 and numero <> 7)")
@DynamicInsert
public class Hexagon {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Min(value = 0, message = "El codi no pot ser menor a cero")
	@Column(name = "codi", nullable = false)
	private Integer codi;
	
	@NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "terreny", nullable = false, length = 10)
    private TipusTerreny terreny;
	
	// null al desert; la resta, de 2 a 12 sense el 7
	@Min(value = 2, message = "El número no pot ser menor a dos")
	@Max(value = 12, message = "El número no pot ser major a dotze")
	@Column(name = "numero")
	private Integer numero; 
	
	@Column(name = "q", nullable = false)
	private Integer q;
	
	@Column(name = "r", nullable = false)
	private Integer r;

	@ManyToOne
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;
	
	@OneToMany(mappedBy = "hexagon")
	@OrderBy("posicio")
	private List<HexagonVertex> hexagonVertexs = new ArrayList<>();

	public Hexagon() {
	}

	public Hexagon(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCodi() {
		return codi;
	}

	public void setCodi(Integer codi) {
		this.codi = codi;
	}

	public TipusTerreny getTerreny() {
		return terreny;
	}

	public void setTerreny(TipusTerreny terreny) {
		this.terreny = terreny;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public Integer getQ() {
		return q;
	}

	public void setQ(Integer q) {
		this.q = q;
	}

	public Integer getR() {
		return r;
	}

	public void setR(Integer r) {
		this.r = r;
	}

	public Partida getPartida() {
		return partida;
	}

	public void setPartida(Partida partida) {
		this.partida = partida;
	}

	public List<HexagonVertex> getHexagonVertexs() {
		return hexagonVertexs;
	}

	public void setHexagonVertexs(List<HexagonVertex> hexagonVertexs) {
		this.hexagonVertexs = hexagonVertexs;
	}

	@Override
	public String toString() {
		return "Hexagon{" +
				"id=" + id +
				", codi=" + codi +
				", terreny=" + terreny +
				", numero=" + numero +
				", q=" + q +
				", r=" + r +
				", partidaId=" + (partida != null ? partida.getId() : null) +
				", hexagonVertexs=" + hexagonVertexs +
				'}';
	}
}
