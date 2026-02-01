# OX Event Plugin

Plugin na serwer Minecraft (Paper 1.21.11) umożliwiający przeprowadzenie eventu OX (Prawda/Fałsz).

## Funkcje

- **Zarządzanie Eventem**: Szybkie rozpoczynanie, kończenie i resetowanie eventu.
- **Pytania**: Tworzenie i zapisywanie pytań w pliku `questions.yml`.
- **Automatyzacja**: 
  - Odliczanie czasu (20s).
  - Wyświetlanie pytań na Action Bar i Boss Bar.
  - Automatyczne usuwanie odpowiedzi (podłogi) i eliminacja graczy.
  - Przywracanie podłogi po rundzie.
- **Fail-safe**:
  - Teleportacja graczy spadających z platformy na widownię.
  - Ochrona przed PvP, niszczeniem bloków i używaniem komend.

## Komendy (Tylko dla Administracji)

- `/ox setspawn` - Ustawia miejsce startu eventu (lobby/platforma).
- `/ox setwidownia` - Ustawia miejsce dla wyeliminowanych graczy.
- `/ox set <o|x>` - Ustawia strefę PRAWDA (O) lub FAŁSZ (X) (wymaga zaznaczenia łopatą).
- `/ox setteleport` - Ustawia strefę fail-safe (jeśli gracz w nią wejdzie, odpada).
- `/ox createquestion <id> <true/false> <tekst>` - Dodaje nowe pytanie.
- `/ox init` - Teleportuje wszystkich graczy na spawn eventu i przygotowuje grę.
- `/ox start` - Rozpoczyna event (wyświetla intro).
- `/ox question <id>` - Zadaje konkretne pytanie i uruchamia odliczanie.
- `/ox end` - Kończy event i teleportuje graczy na spawn w world.

## Instalacja

1. Wrzuć plik `.jar` do folderu `plugins/`.
2. Zrestartuj serwer.
3. Skonfiguruj strefy używając drewnianej łopaty i komend `/ox set...`.

## Licencja

MIT
