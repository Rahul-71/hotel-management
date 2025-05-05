package com.telus.hotel_management.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleType name;

    public Role() {
    }

    public Role(RoleType role) {
        this.name = role;
    }

    public Long getId() {
        return id;
    }

    public RoleType getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(RoleType name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name=" + name +
                '}';
    }
}

