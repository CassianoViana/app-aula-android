package br.com.trivio.wms.data.model;

public enum TaskStatus {

  UNDEFINED("Indefinido", "red"),
  PENDING("Pendente", "#E0A752"),
  DOING("Iniciada", "#118FE4"),
  DONE("Concluída", "#1EAE50"),
  CANCELLED("Cancelada", "#E23659"),
  ;
  final String name;

  final String color;

  TaskStatus(String name, String color) {
    this.name = name;
    this.color = color;
  }

  public boolean isEnded() {
    return this == DONE || this == CANCELLED;
  }
}

