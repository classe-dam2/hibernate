package models.abstracts;

import jakarta.persistence.*;
import models.Vertex;

@Entity
public abstract class Assentament extends Construccio {
    // EAGER: una sola referència; un assentament sempre es mostra o es comprova amb el seu vèrtex
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vertex_id", unique = true, nullable = false)
    private Vertex vertex;

    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
        // Mantenim els dos costats de la relació sincronitzats
        if (vertex != null) {
            vertex.setAssentament(this);
        }
    }

    @Override
    public String toString() {
        return "Assentament{" +
                "vertexId=" + (vertex != null ? vertex.getId() : null) +
                '}';
    }
}
