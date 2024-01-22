package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import pt.tecnico.uilib.forms.Form;
import ggc.exceptions.MissingFileAssociationException;
import java.io.IOException;
import ggc.WarehouseManager;
//FIXME import classes

/**
 * Save current state to file under current name (if unnamed, query for name).
 */
class DoSaveFile extends Command<WarehouseManager> {

  private String name;

  /** @param receiver */
  DoSaveFile(WarehouseManager receiver) {
    super(Label.SAVE, receiver);
    }
  

  @Override
  public final void execute() throws CommandException {
    try {
      _receiver.save();
    } 

     catch (MissingFileAssociationException e){
       try {
         String name = Form.requestString(Prompt.newSaveAs());
         _receiver.saveAs(name);
       }
       catch (IOException | MissingFileAssociationException io){
         io.printStackTrace();
       }
    }
    catch (IOException e){
      e.printStackTrace();
    }
  }
}
