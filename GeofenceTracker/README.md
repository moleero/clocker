# GeofenceTracker

Minimalna, działająca appka jak Geofency: dodajesz strefy (lat/lng/promień), appka w tle
loguje wejście i wyjście z każdej strefy oraz czas spędzony w środku. Eksport do CSV.

## Jak uruchomić

1. Otwórz folder w Android Studio (File → Open → wskaż `GeofenceTracker/`).
2. Poczekaj aż Gradle pobierze zależności.
3. Podłącz telefon (lub emulator z Google Play Services) i uruchom `app`.

## Pierwsze użycie

1. Dodaj strefę: nazwa + współrzędne (skopiuj z Google Maps – przytrzymaj punkt na
   mapie, na dole wyskoczą lat/lng) + promień w metrach.
2. Zaakceptuj uprawnienia lokalizacji. **Kluczowe**: gdy system zapyta o dostęp do
   lokalizacji, wybierz "Zezwalaj cały czas" (nie "tylko podczas używania") — inaczej
   geofencing w tle nie zadziała.
3. Ręcznie wyłącz optymalizację baterii dla appki: Ustawienia → Aplikacje →
   GeofenceTracker → Bateria → Brak ograniczeń. Bez tego Android po jakimś czasie
   ubije proces i stracisz część wejść/wyjść.
4. Historię i eksport CSV znajdziesz pod przyciskiem "Historia".

## Jak to działa

- `GeofenceHelper` rejestruje strefy w Google Play Services Geofencing API (system
  Android sam pilnuje geofence, nawet gdy appka nie działa — to najbardziej
  energooszczędna i niezawodna metoda, ta sama której używa Geofency).
- `GeofenceBroadcastReceiver` łapie zdarzenia ENTER/EXIT, liczy czas i zapisuje do
  lokalnej bazy Room.
- `TrackingForegroundService` trzyma cichą powiadomienie, żeby system rzadziej
  zabijał proces appki.
- `BootReceiver` odtwarza strefy po restarcie telefonu (geofence rejestracje giną
  po reboot).

## Ograniczenia tej wersji (MVP)

- Dodawanie strefy przez wpisanie współrzędnych ręcznie — nie ma wbudowanej mapy do
  klikania. Dodanie mapy: podmień dialog na `SupportMapFragment` z
  `com.google.android.gms:play-services-maps` i własnym kluczem API Google Maps.
- Brak logowania w tle na urządzeniach z bardzo agresywnym zarządzaniem baterią
  (np. część telefonów Xiaomi/Huawei) bez dodatkowych ustawień producenta — to
  ograniczenie systemowe, nie appki.
- Loitering delay ustawiony na 30s (żeby GPS nie "migał" fałszywych wejść) — można
  zmienić w `GeofenceHelper.registerZone`.
