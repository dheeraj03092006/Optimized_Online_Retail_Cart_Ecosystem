class Customer {
    private int id;
    private String name;
    private String email;
    private String address;

    public Customer(String name, String email, String address) {
        this.name = name;
        this.email = email;
        this.address = address;
    }

    // Getters and setters...
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name='" + name + "', email='" + email + "', address='" + address + "'}";
    }

    String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    String getEmail() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    String getAddress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}