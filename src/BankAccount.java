import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Account {
    int balance = 0;
    final ReentrantLock lock = new ReentrantLock();
    final Condition condition = lock.newCondition();

    // Метод для пополнения баланса
    void add(int sum) {
        lock.lock();
        try {
            balance += sum;
            System.out.println("Пополнение: +" + sum + " руб. Баланс: " + balance + " руб.");
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // Метод для снятия денег
    void takeMoney(int sum) {
        lock.lock();
        try {
            balance -= sum;
            System.out.println("Снятие: -" + sum + " руб. Баланс: " + balance + " руб.");
        } finally {
            lock.unlock();
        }
    }

    // Метод для циклического ожидания и снятия
    void waitAndWithdraw(int targetSum, int withdrawAmount, int maxOperations) throws InterruptedException {
        lock.lock();
        try {
            for (int i = 0; i < maxOperations; i++) {
                System.out.println("[" + (i + 1) + "] Ожидаем накопления " + targetSum + " руб. для снятия...");

                while (balance < targetSum) {
                    condition.await();
                }

                System.out.println("[" + (i + 1) + "] Целевая сумма " + targetSum + " руб. достигнута!");

                // Снимаем деньги
                takeMoney(withdrawAmount);

                System.out.println("[" + (i + 1) + "] Операция завершена. Баланс: " + balance + " руб.");
                System.out.println("---");
            }
        } finally {
            lock.unlock();
        }
    }
}

// Поток для пополнения счета
class Deposit extends Thread {
    Account account;
    Random random = new Random();
    boolean running = true;

    Deposit(Account account) {
        this.account = account;
    }

    @Override
    public void run() {
        try {
            int operationCount = 0;
            while (running && operationCount < 20) { // Увеличим количество пополнений
                int amount = random.nextInt(100) + 1;
                account.add(amount);
                Thread.sleep(500);
                operationCount++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void stopDeposit() {
        running = false;
    }
}

// Главный класс
public class BankAccount {
    public static void main(String[] args) {
        Account account = new Account();
        Scanner in = new Scanner(System.in);

        // Ввод данных
        System.out.print("Введите целевую сумму для накопления (a): ");
        int a = in.nextInt();

        System.out.print("Введите сумму для снятия (b): ");
        int b = in.nextInt();

        System.out.print("Введите количество операций снятия: ");
        int operations = in.nextInt();

        // Запускаем поток для пополнения счета
        Deposit depositThread = new Deposit(account);
        depositThread.start();

        try {

            account.waitAndWithdraw(a, b, operations);
            depositThread.stopDeposit();
            depositThread.join();

            System.out.println("Все операции завершены!");
            System.out.println("Итоговый баланс: " + account.balance + " руб.");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
    }
}