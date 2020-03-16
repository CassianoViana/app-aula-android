package br.com.trivio.wms.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TaskStatus {

  UNDEFINED("Indefinido", "red"),
  PENDING("Pendente", "yellow"),
  DOING("Iniciada", "blue"),
  DONE("Concluída", "green"),
  CANCELLED("Cancelada", "red"),
  ;

  private final String name;
  private final String colorClass;

  TaskStatus(String name, String colorClass) {
    this.name = name;
    this.colorClass = colorClass;
  }

  public String getColorClass() {
    return colorClass;
  }

  public String getName() {
    return name;
  }
}
