package ggc.app.main;

import ggc.exceptions.NegativeDateException;
import ggc.app.exceptions.InvalidDateException;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.WarehouseManager;

/**
 * Advance current date.
 */
class DoAdvanceDate extends Command<WarehouseManager> {

  DoAdvanceDate(WarehouseManager receiver) {
    super(Label.ADVANCE_DATE, receiver);
    addIntegerField("numberOfDays",Prompt.daysToAdvance());
  }

  @Override
  public final void execute() throws CommandException {
    try {
      int p = integerField("numberOfDays");
      _receiver.advanceDate(p);
    }   catch (NegativeDateException e) {
      throw new InvalidDateException(e.getDays());
    }
  }

}
