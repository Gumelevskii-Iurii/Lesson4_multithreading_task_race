import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static final int CARS_COUNT = 4;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("������ ���������� >>> ����������!!!");
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];
        Thread[] threads = new Thread[CARS_COUNT];
        for (int i = 0; i < cars.length; i++) {
            cars[i] = new Car(race, 20 + (int) (Math.random() * 10));
        }
        for (int i = 0; i < cars.length; i++) {
            threads[i] = new Thread(cars[i]);
            threads[i].start();
        }
        for (int i = 0; i < cars.length; i++) {
            threads[i].join();
        }
        System.out.println("������ ���������� >>> ����� �����������!!!");
    }
}

class Car implements Runnable {
    static Car winner;
    Lock lock = new ReentrantLock();
    private static final CountDownLatch cdl = new CountDownLatch(4);
    private static int CARS_COUNT;
    private Race race;
    private int speed;
    private String name;

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public Car(Race race, int speed) {
        this.race = race;
        this.speed = speed;
        CARS_COUNT++;
        this.name = "�������� #" + CARS_COUNT;
    }

    @Override
    public void run() {

        try {
            System.out.println(this.name + " ���������");
            Thread.sleep(500 + (int) (Math.random() * 800));
            System.out.println(this.name + " �����");
            cdl.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("������ ���������� >>> ����� ��������!!!");
        cdl.countDown();

        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this);
        }

        try {
            lock.lock();
            if (isWinner(this)) System.out.println(getName() + " - WIN");
        } finally {
            lock.unlock();
        }
    }

    public static boolean isWinner(Car c) {
        if (winner == null) {
            winner = c;
            return true;
        }
        return false;
    }
}


abstract class Stage {
    protected int length;
    protected String description;

//    public String getDescription() {
//        return description;
//    }

    public abstract void go(Car c);
}

class Road extends Stage {
    public Road(int length) {
        this.length = length;
        this.description = "������ " + length + " ������";
    }

    @Override
    public void go(Car c) {
        try {
            System.out.println(c.getName() + " ����� ����: " + description);
            Thread.sleep(length / c.getSpeed() * 1000);
            System.out.println(c.getName() + " �������� ����: " + description);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Tunnel extends Stage {
    Semaphore smp = new Semaphore(2);

    public Tunnel() {
        this.length = 80;
        this.description = "������� " + length + " ������";
    }

    @Override
    public void go(Car c) {
        try {
            try {
                System.out.println(c.getName() + " ��������� � �����(����): " + description);
                smp.acquire();
                System.out.println(c.getName() + " ����� ����: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(c.getName() + " �������� ����: " + description);
                smp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Race {
    private Vector<Stage> stages;

    public Vector<Stage> getStages() {
        return stages;
    }

    public Race(Stage... stages) {
        this.stages = new Vector<>(Arrays.asList(stages));
    }
}

