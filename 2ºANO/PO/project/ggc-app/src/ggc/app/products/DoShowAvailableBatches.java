package ggc.app.products;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.WarehouseManager;
import java.util.ArrayList;
import java.text.Collator;
import java.util.Locale;

/**
 * Show available batches.
 */
class DoShowAvailableBatches extends Command<WarehouseManager> {

  DoShowAvailableBatches(WarehouseManager receiver) {
    super(Label.SHOW_AVAILABLE_BATCHES, receiver);
  }

  @Override
  public final void execute() throws CommandException {
    ArrayList<String> lista = _receiver.showAllAvailableBatches();
    for (String s:lista){
      _display.popup(s);
    }
  }

}
