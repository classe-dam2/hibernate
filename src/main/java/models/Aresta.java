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

	// LAZY: referència de tornada; s'hi arriba sempre des de la partida
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;
	
	// EAGER: els dos extrems són dos objectes petits que es consulten a cada comprovació de carreteres i veïns
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "vertex_a_id", nullable = false)
	private Vertex vertexA;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "vertex_b_id", nullable = false)
	private Vertex vertexB;

	public Aresta() {
	}

	// EAGER: al costat invers d'una 1:1 el LAZY no funciona (Hibernate ha de fer la SELECT per saber si és null)
	@OneToOne(mappedBy = "aresta", fetch = FetchType.EAGER)
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

	// Iguals si tenen el mateix id; una entitat nova (id null) només és igual a ella mateixa.
	// Es fa servir getId() i no el camp perquè en un proxy els camps són buits.
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Aresta other)) return false;
		return getId() != null && getId().equals(other.getId());
	}

	// Constant: no pot canviar quan l'entitat rep l'id, ni dependre de la classe d'un proxy
	@Override
	public int hashCode() {
		return Aresta.class.hashCode();
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
