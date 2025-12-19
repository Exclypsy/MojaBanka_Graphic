
# Moja Banka (Java + JavaFX + MySQL)

Projekt **Moja Banka** je banková aplikácia vytvorená v jazyku **Java**, s grafickým rozhraním **JavaFX**, databázou **MySQL** a konzolovou verziou aplikácie. Projekt demonštruje použitie **OOP princípov**, **MVC architektúry**, **DAO vrstvy** a **prístupových práv používateľov**.

---

## 1. Princípy OOP a ich použitie v projekte

### 1.1 Trieda, objekt, zapúzdrenie

Objektovo orientované programovanie (OOP) pracuje s pojmami **trieda** a **objekt**. Trieda je šablóna, ktorá popisuje, aké údaje (vlastnosti) a správanie (metódy) bude mať konkrétny objekt. Objekt je konkrétna inštancia triedy, teda „živý“ záznam s konkrétnymi hodnotami.

Zapúzdrenie (enkapsulácia) znamená, že údaje a operácie nad nimi sú zabalené v jednej triede a vonkajší kód pristupuje k údajom cez metódy, nie priamo.

V projekte **Moja Banka** je príkladom trieda **Ucet**, ktorá obsahuje vlastnosti:
```
- `id`
- `owner_name`
- `number`
- `balance`
- `interest`

a metódy:

- `vklad()`
- `vyber()`
- `zapocitajUrok()`
```
Vonkajší kód nemení zostatok priamo, ale vždy cez tieto metódy, čím sa zabezpečí kontrola pravidiel (kontrola zostatku, limitu a pod.).

```java
public class Ucet {
    private long id;
    private String owner_name;
    private long number;
    private double balance;
    private double interest;

    public double getBalance() {
        return balance;
    }

    public void vklad(double suma) {
        if (suma <= 0) throw new IllegalArgumentException("Suma musí byť > 0");
        this.balance += suma;
    }

    public void vyber(double suma) {
        if (suma > balance) throw new IllegalArgumentException("Nedostatočný zostatok");
        this.balance -= suma;
    }
}
````

Podobne trieda **User** zapúzdruje údaje o používateľovi (`id`, `username`, `password_hash`, `role`, `fullName`) a poskytuje metódy na prácu s databázou.

---

### 1.2 Dedičnosť a rozšírenie správania

Dedičnosť umožňuje vytvoriť novú triedu, ktorá preberá vlastnosti a metódy z existujúcej triedy a môže ich rozšíriť alebo upraviť.

Základnou triedou je **Ucet**, z ktorej dedí trieda **UcetDoMinusu**. Táto trieda:

* rozširuje účet o `overdraft_limit` a `overdraft_interest`,
* prepisuje metódu `vyber()` tak, aby bolo možné ísť do mínusu.

```java
public class UcetDoMinusu extends Ucet {
    private double overdraft_limit;
    private double overdraft_interest;

    @Override
    public void vyber(double suma) {
        double min_dovolen = -overdraft_limit;
        if (balance - suma < min_dovolen) {
            throw new IllegalArgumentException("Prekročíš limit prečerpania");
        }
        this.balance -= suma;
    }
}
```

---

### 1.3 Polymorfizmus a spoločné rozhranie

Polymorfizmus znamená, že rôzne triedy majú rovnaké metódy, ale ich implementácia sa môže líšiť.

V projekte majú triedy **Ucet** aj **UcetDoMinusu** rovnaké metódy (`vklad()`, `vyber()`, `zapocitajUrok()`), vďaka čomu môže aplikačná logika pracovať s oboma typmi účtov jednotne.

```java
List<Ucet> accounts = accountDao.findByUserId(userId);
for (Ucet acc : accounts) {
    acc.vklad(100);
}
```

---

### 1.4 Prepojenie OOP a databázy

Každá trieda reprezentuje jednu tabuľku v databáze:

* `User` ↔ `users`
* `Ucet` / `UcetDoMinusu` ↔ `accounts`

Objekty sa vytvárajú v Jave a ukladajú alebo načítavajú cez DAO vrstvu pomocou SQL príkazov.

---

## 2. Použitie SQL databázy v Jave

### 2.1 Pripojenie k MySQL

Projekt používa **JDBC** a triedu **Db**, ktorá zabezpečuje pripojenie k databáze.

Typický postup práce s DB:

1. Získanie `Connection`
2. Vytvorenie `PreparedStatement`
3. Nastavenie parametrov
4. Vykonanie SQL
5. Spracovanie `ResultSet`

---

### 2.2 Databázové tabuľky

Použité tabuľky:

* **users** – používatelia
* **account_types** – typy účtov
* **accounts** – bankové účty
* **transactions** – záznamy operácií

Cudzie kľúče zabezpečujú integritu údajov medzi používateľmi a účtami.

---

### 2.3 Aktualizácia údajov a logovanie

Každá finančná operácia sa zapisuje do tabuľky **transactions**.

```java
public void updateBalance(long accountId, double newBalance) throws SQLException {
    String sql = "UPDATE accounts SET balance=? WHERE id=?";
    try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setBigDecimal(1, BigDecimal.valueOf(newBalance));
        ps.setLong(2, accountId);
        ps.executeUpdate();
    }
}
```

---

## 3. Architektúra MVC a JavaFX

### 3.1 Model

Model obsahuje:

* `model/` – User, Ucet, UcetDoMinusu
* `dao/` – Db, UserDao, AccountDao, TransactionDao
* `Session`
* `AuthService`

---

### 3.2 View

View vrstva pozostáva z:

* FXML súborov (`login-view.fxml`, `user-dashboard.fxml`, `admin-dashboard.fxml`)
* CSS štýlov

---

### 3.3 Controller

Controllery spracúvajú udalosti z GUI a volajú model.

```java
@FXML
private void onLogin() {
    String username = usernameField.getText();
    String password = passwordField.getText();

    User u = userDao.findByUsernameAndPassword(username, password);
    if (u == null) {
        statusLabel.setText("Nesprávne meno alebo heslo.");
        return;
    }

    Session.set(u);
    if ("ADMIN".equalsIgnoreCase(u.getRole())) {
        openAdminDashboard();
    } else {
        openUserDashboard();
    }
}
```

---

## 4. Prístupové práva a roly

### Admin

* správa používateľov
* správa účtov
* globálny prehľad transakcií
* započítanie úrokov

### Bežný používateľ

* vidí len svoje účty
* môže vkladať a vyberať
* nemá prístup k administrácii

---

## 5. Konzolové rozhranie

Projekt obsahuje aj konzolovú aplikáciu.

```java
public class ConsoleApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("=== Moja Banka – konzola ===");
            System.out.println("1) Prihlásiť");
            System.out.println("0) Koniec");
            String volba = sc.nextLine();

            if ("0".equals(volba)) break;
            else if ("1".equals(volba)) ConsoleLogin.handleLogin(sc);
        }
    }
}
```

---

## 6. Zhrnutie

Projekt demonštruje:

* OOP princípy
* DAO a JDBC
* JavaFX MVC architektúru
* Zdieľanú logiku pre GUI aj konzolu
* Prístupové práva používateľov
* Auditný log transakcií

Aplikácia je plne funkčný bankový systém s databázovým backendom.

---

## 7. Screenshoty

* Konzola
<img width="3598" height="2258" alt="image" src="https://github.com/user-attachments/assets/e17c4b17-0146-4955-922a-03c775504ae3" />

* Databáza
<img width="3440" height="2086" alt="image" src="https://github.com/user-attachments/assets/dbcebc34-ee1e-41fe-a926-c3d0590fde61" />

* Úvodná obrazovka
<img width="2576" height="1862" alt="image" src="https://github.com/user-attachments/assets/28edd44e-6cde-4b20-9db7-39de4e4b50f1" />

* Admin panel
<img width="2566" height="1868" alt="image" src="https://github.com/user-attachments/assets/c409efc5-6444-4302-9ae9-b59692467563" />

* Klient panel
<img width="2560" height="1856" alt="image" src="https://github.com/user-attachments/assets/28cfc48b-7073-4bf1-85e1-948dedfd9f44" />


```
```
