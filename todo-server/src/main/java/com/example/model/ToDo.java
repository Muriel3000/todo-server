package com.example.model;

import java.time.LocalDate;

public record ToDo(int id, String title, LocalDate date, int priority, boolean completed) {}
