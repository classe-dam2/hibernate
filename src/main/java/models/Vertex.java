package models;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

	// LAZY: referència de tornada; s'hi arriba sempre des de la partida
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;

	// Costat invers de Assentament.vertex: la construcció d'aquest vèrtex (null si està lliure)
	// EAGER: al costat invers d'una 1:1 el LAZY no funciona (Hibernate ha de fer la SELECT per saber si és null)
	@OneToOne(mappedBy = "vertex", fetch = FetchType.EAGER)
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

	// Iguals si tenen el mateix id; una entitat nova (id null) només és igual a ella mateixa.
	// Es fa servir getId() i no el camp perquè en un proxy els camps són buits.
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Vertex other)) return false;
		return getId() != null && getId().equals(other.getId());
	}

	// Constant: no pot canviar quan l'entitat rep l'id, ni dependre de la classe d'un proxy
	@Override
	public int hashCode() {
		return Vertex.class.hashCode();
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
