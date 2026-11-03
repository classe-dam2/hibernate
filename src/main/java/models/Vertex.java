package models;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import models.abstracts.Assentament;

@Entity
@Table(name = "vertex")
@DynamicInsert
public class Vertex {
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

	// Costat invers de Assentament.vertex: la construcció d'aquest vèrtex (null si està lliure)
	@OneToOne(mappedBy = "vertex")
	private Assentament assentament;

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

	public Assentament getAssentament() {
		return assentament;
	}

	public void setAssentament(Assentament assentament) {
		this.assentament = assentament;
	}

	@Override
	public String toString() {
		return "Vertex{" +
				"id=" + id +
				", codi=" + codi +
				", partidaId=" + (partida != null ? partida.getId() : null) +
				'}';
	}

	public static boolean mateix(Vertex x, Vertex y) {
		return x != null && y != null && x.getId().equals(y.getId());
	}
}
