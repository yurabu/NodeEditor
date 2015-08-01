package com.yurabu.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Represent node in a tree
 * 
 * @author Yurii
 * 
 */
@Entity
public class Node implements Serializable {
	private static final long serialVersionUID = -6425366814448671541L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long number;
	
	@ManyToOne
	private Node parent;
	
	public Node() {
	}
	
	public Node(Long number) {
		this.number = number;
	}
	
	public Node(Node parent) {
		this.parent = parent;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Long getNumber() {
		return number;
	}

	public void setNumber(Long number) {
		this.number = number;
	}
}
