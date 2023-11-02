package com.azureAccelerator.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "role_id")
		private Integer id;

		@Column(nullable = false, length = 45)
		private String name;

		public Role() { }

		public Role(String name) {
			this.name = name;
		}

		public Role(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public Role(Integer id) {
			this.id = id;
		}


		@Override
		public String toString() {
			return this.name;
		}

		// getters and setters are not shown for brevity
	}

