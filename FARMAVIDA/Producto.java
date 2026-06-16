import java.io.Serializable;

public class Producto implements Serializable {
    // 1. Atributos
    private String nombre;
    private double precio;

    // 2. Constructor
    public Producto(String nombre, double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    // 3. Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    // 4. Método para mostrar información
    public void mostrarInfo() {
        System.out.println("Producto: " + nombre + ", Precio: " + precio + " USD");
    }
}
