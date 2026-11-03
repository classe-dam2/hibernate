package models;

import jakarta.persistence.*;
import models.abstracts.Carretera;
import org.hibernate.annotations.DynamicInsert;

import jakarta.validation.constraints.Min;

@Entity
@Table(name = "aresta")
@DynamicInsert
public class Aresta {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Min(value = 0, message = "El codi no pot ser menor a cero")
	@Column(name = "codi", nullable = false)
	private Integer codi;

	@ManyToOne
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;
	
	@ManyToOne
	@JoinColumn(name = "vertex_a_id", nullable = false)
	private Vertex vertexA;
	
	@ManyToOne
	@JoinColumn(name = "vertex_b_id", nullable = false)
	private Vertex vertexB;

	public Aresta() {
	}

	@OneToOne(mappedBy = "aresta")
	private Carretera carretera;   // null si està lliure

	public Carretera getCarretera() { return carretera; }

public void setCarretera(Carretera carretera) { this.carretera = carretera; }

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

	public Partida getPartida() {
		return partida;
	}

	public void setPartida(Partida partida) {
		this.partida = partida;
	}

	public Vertex getVertexA() {
		return vertexA;
	}

	public void setVertexA(Vertex vertexA) {
		this.vertexA = vertexA;
	}

	public Vertex getVertexB() {
		return vertexB;
	}

	public void setVertexB(Vertex vertexB) {
		this.vertexB = vertexB;
	}

	@Override
	public String toString() {
		return "Aresta{" +
				"id=" + id +
				", codi=" + codi +
				", partidaId=" + (partida != null ? partida.getId() : null) +
				", vertexAId=" + (vertexA != null ? vertexA.getId() : null) +
				", vertexBId=" + (vertexB != null ? vertexB.getId() : null) +
				'}';
	}
}
