//TIP Aby <b>uruchomić</b> kod, naciśnij <shortcut actionId="Run"/> lub
// Kliknij ikonę <icon src="AllIcons.Actions.Execute"/> na marginesie.
void main() {
    //TIP Naciśnij <shortcut actionId="ShowIntentionActions"/>, gdy kursor znajduje się na wyróżnionym tekście
    // aby zobaczyć, jak IntelliJ IDEA sugeruje to naprawić.
    IO.println(String.format("Hello and welcome siema!"));

    for (int i = 1; i <= 5; i++) {
        //TIP Naciśnij <shortcut actionId="Debug"/>, aby rozpocząć debugowanie kodu. Ustawiliśmy jeden punkt przerwania <icon src="AllIcons.Debugger.Db_set_breakpoint"/>
        // ale zawsze możesz dodać ich więcej, naciskając <shortcut actionId="ToggleLineBreakpoint"/>.
        IO.println("i = " + i);
    }
}
