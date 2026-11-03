package models;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "hexagonvertex", uniqueConstraints = @UniqueConstraint(columnNames = {"id_hexagon", "id_vertex"}))
@Check(constraints = "posicio between 0 and 5")
@DynamicInsert
public class HexagonVertex {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Min(value = 0, message = "La posicio no pot ser menor a cero")
	@Max(value = 5, message = "La posicio no pot ser major a cinc")
	@Column(name = "posicio", nullable = false)
	private Integer posicio;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_hexagon", nullable = false)
	private Hexagon hexagon;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_vertex", nullable = false)
	private Vertex vertex;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPosicio() {
		return posicio;
	}

	public void setPosicio(Integer posicio) {
		this.posicio = posicio;
	}

	public Hexagon getHexagon() {
		return hexagon;
	}

	public void setHexagon(Hexagon hexagon) {
		this.hexagon = hexagon;
	}

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}

	@Override
	public String toString() {
		return "HexagonVertex{" +
				"id=" + id +
				", posicio=" + posicio +
				", hexagonId=" + (hexagon != null ? hexagon.getId() : null) +
				", vertexId=" + (vertex != null ? vertex.getId() : null) +
				'}';
	}
}
