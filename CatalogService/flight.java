import java.util.*;
import java.lang.*;
import java.io.*;

import java.util.Scanner;

class FlightDetails {
    String flightName;
    String SourceCity;
    String DestinationCity;
    int Capacity = 50;

    int availableSeats = Capacity;
    int booked = 0;
    
    public FlightDetails(String flightName, String SourceCity, String DestinationCity) {
        this.flightName = flightName;
        this.SourceCity = SourceCity;
        this.DestinationCity = DestinationCity;
    }

    void setFlightName(String flightName) {
        this.flightName = flightName;
    }

    void setSourceCity(String SourceCity) {
        this.SourceCity = SourceCity;
    }

    void setDestinationCity(String DestinationCity) {
        this.DestinationCity = DestinationCity;
    }
    void setFlightCapacity(int Capacity) {
        this.Capacity = Capacity;
    }

    int getFlightCapacity() {
        return Capacity;
    }
    
    void display() {
        System.out.println(flightName + " " + SourceCity + " " + DestinationCity + " " + booked + " " + availableSeats);
    }

    String flightBook(int passengers) {
        if(passengers > getFlightCapacity()) {
            return "The Flight is full";
        } else {
            setFlightCapacity(getFlightCapacity() - passengers);
            booked += passengers;
            availableSeats -= passengers;
            return "Booking Done";
        }
    }
}




public class Main {
    public static void main (String[] args) {
        FlightDetails dayOne = new FlightDetails("AIR20", "Delhi", "Mumbai");
        Scanner sc = new Scanner(System.in);
        List<FlightDetails> arr = new ArrayList<FlightDetails>();
        System.out.println("Enter the number of passengers:");
        int passengers = sc.nextInt();
        System.out.println(dayOne.flightBook(passengers));
        
        arr.add(dayOne);
        
        FlightDetails dayTwo = new FlightDetails("AIR20", "Delhi", "Mumbai");
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the number of passengers:");
        int passengers = sc.nextInt();
        System.out.println(dayTwo.flightBook(passengers));
        
        arr.add(dayTwo);
        
        arr.forEach(flight -> flight.display()); 
    }
}