package com.example.activitymonitor.models;

public class User {
    private int id;
    private String email;
    private String password;
    private String name;
    private int age;
    private double weight;
    private double height;
    private String gender;
    private String photoPath;
    private String createdAt;
    private Goal goal;


    // Конструктор за замовчуванням
    public User() {}

    // Конструктор з усіма полями
    public User(int id, String email, String name, int age, double weight, double height, String gender) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.gender = gender;
    }
    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }


    // Конструктори, гетери та сетери...

    public double calculateBMI() {
        if (height > 0) {
            return weight / ((height / 100) * (height / 100));
        }
        return 0;
    }

    public String getBMICategory() {
        double bmi = calculateBMI();
        if (bmi < 18.5) return "Недостатня вага";
        else if (bmi < 25) return "Нормальна вага";
        else if (bmi < 30) return "Надлишкова вага";
        else return "Ожиріння";
    }


    // Гетери і сетери для ВСІХ полів
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}