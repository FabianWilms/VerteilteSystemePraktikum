package table;

import api.BindingProxy;
import api.Manager;
import api.TablePart;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by René Zarwel on 01.06.2016.
 */
public class TablePartImpl implements TablePart {

  private static final Logger LOG = Logger.getLogger(TablePart.class.getName());


  private final String id = UUID.randomUUID().toString();

  private Fork leftFork = new Fork();
  private Fork rightFork = new Fork();

  private TablePart nextTablePart;

  public TablePartImpl(String ip) {

    super();
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager());
    }
    try {
      //Register on Registry
      TablePart stub = (TablePart) UnicastRemoteObject.exportObject(this, 0);
      Registry registry = LocateRegistry.getRegistry(ip);
      BindingProxy bindingProxy = (BindingProxy) registry.lookup(BindingProxy.NAME);
      bindingProxy.proxyRebind(id, stub);

      LOG.log(Level.INFO, String.format("TablePart %s bound to registry.", id));

      Manager manager = (Manager) registry.lookup(Manager.NAME);
      manager.registerTablepart(id);

      LOG.log(Level.INFO, String.format("TablePart %s registered in manager.", id));

      nextTablePart = manager.getNextTablePart(id);

    } catch (Exception e) {
      LOG.log(Level.SEVERE, String.format("Problem binding TablePart %s.", id));
      throw new RuntimeException(e.getMessage());
    }


  }

  @Override
  public Map<TablePart, Integer> takeSeat(String uuid) throws RemoteException {

    Map<TablePart, Integer> result = new LinkedHashMap<>();

    //Try to get both forks
    if(takeLeftFork(uuid) && takeRightFork(uuid)){
      result.put(this, 0);
      result.put(this, 1);
      return result;

    } else {
      //Dont got both forks, free and move further
      leftFork.unblock();

      //Try to get right fork and left fork of next TP
      if(takeRightFork(uuid)){

        if(nextTablePart.takeLeftFork(uuid)){
          result.put(this, 1);
          result.put(nextTablePart, 0);
          return result;
        } else {
          rightFork.unblock();
        }
      }

    }

    result.put(nextTablePart, null);
    return result;
  }

  @Override
  public boolean takeLeftFork(String uuid) throws RemoteException {
    return leftFork.tryBlock(uuid);
  }

  @Override
  public boolean takeRightFork(String uuid) throws RemoteException {
    return rightFork.tryBlock(uuid);
  }

  @Override
  public void leaveSeat(Integer seatNumber) throws RemoteException {
    switch (seatNumber) {
      case 0: leftFork.unblock();
        break;
      case 1: rightFork.unblock();
        break;
    }
  }

  @Override
  public void setNextTablePart(TablePart nextTablePart) throws RemoteException {
    this.nextTablePart = nextTablePart;
  }

  @Override
  public String getId() throws RemoteException {
    return id;
  }
}
