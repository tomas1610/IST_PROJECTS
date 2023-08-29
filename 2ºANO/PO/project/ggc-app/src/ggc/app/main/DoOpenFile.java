package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.exceptions.UnavailableFileException;
import ggc.app.exceptions.FileOpenFailedException;
import ggc.app.main.Prompt;
import ggc.WarehouseManager;
//FIXME import classes

/**
 * Open existing saved state.
 */
class DoOpenFile extends Command<WarehouseManager> {

  private String name;
  /** @param receiver */
  DoOpenFile(WarehouseManager receiver) {
    super(Label.OPEN, receiver);
    addStringField("fileName",Prompt.openFile());
  }

  @Override
  public final void execute() throws CommandException {
    try {
      name = stringField("fileName");
      _receiver.load(name);
    } catch (UnavailableFileException ufe) {
      throw new FileOpenFailedException(ufe.getFilename());
    }
    
  }

}
