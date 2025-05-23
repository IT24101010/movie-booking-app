package com.PGN15.movie_booking_app.dsa;


import com.hiruna.movieticketbooking.model.Booking;
import java.util.NoSuchElementException;


public class ManualBookingQueue {
    private Booking[] queueArray;
    private int front;      // Index of the front element
    private int rear;       // Index of the rear element
    private int nItems;     // Current number of items in the queue
    private int maxSize;    // Maximum capacity of the queue


    public ManualBookingQueue(int size) {
        this.maxSize = size;
        this.queueArray = new Booking[maxSize];
        this.front = 0;
        this.rear = -1; // Rear starts at -1, indicating an empty queue
        this.nItems = 0;
    }


    public void enqueue(Booking booking) {
        if (isFull()) {
            System.err.println("Queue is full. Cannot enqueue booking ID: " + (booking != null ? booking.getBookingId() : "N/A"));
            return;
        }
        if (rear == maxSize - 1) {
            rear = -1;
        }
        queueArray[++rear] = booking; // Increment rear and then insert
        nItems++;
        System.out.println("Enqueued booking for user: " + booking.getUserId() + ". Queue size: " + nItems);
    }


    public Booking dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty. Cannot dequeue.");
        }
        Booking temp = queueArray[front++]; // Get the front element and then increment front
        if (front == maxSize) {
            front = 0;
        }
        nItems--;
        System.out.println("Dequeued booking. Queue size: " + nItems);
        return temp;
    }



    public Booking peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty. Cannot peek.");
        }
        return queueArray[front];
    }


    public boolean isEmpty() {
        return (nItems == 0);
    }


    public boolean isFull() {
        return (nItems == maxSize);
    }


    public int size() {
        return nItems;
    }
}