package org.bimrocket.ihub.util;

import java.io.InputStream;
import java.util.UUID;

import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;

@Service
public class InventoryUtils
{
  public String compress(String uuid)
  {
    PythonInterpreter pythonInterpreter = new PythonInterpreter();
    InputStream is = getClass().getResourceAsStream("/python/guid.py");
    pythonInterpreter.execfile(is);
    pythonInterpreter.set("input_uuid", uuid.replaceAll("-", ""));
    pythonInterpreter.exec("x = compress(input_uuid)");
    return pythonInterpreter.get("x").asString();
  }

  public String expand(String globalId)
  {
    PythonInterpreter pythonInterpreter = new PythonInterpreter();
    InputStream is = getClass().getResourceAsStream("/python/guid.py");
    pythonInterpreter.execfile(is);
    pythonInterpreter.set("global_id", globalId);
    pythonInterpreter.exec("x = expand(global_id)");
    return pythonInterpreter.get("x").asString();
  }

  public String getGuid()
  {
    PythonInterpreter pythonInterpreter = new PythonInterpreter();
    InputStream is = getClass().getResourceAsStream("/python/guid.py");
    pythonInterpreter.execfile(is);
    pythonInterpreter.exec("x = new()");
    return pythonInterpreter.get("x").asString();
  }

  public static void main(String[] args)
  {
    InventoryUtils utils = new InventoryUtils();

    for (int k = 0; k < 4; k++)
    {
      String uuid = UUID.randomUUID().toString();
      System.out.println("UUID: " + uuid);

      String ifc1 = utils.compress(uuid);
      String uuid1 = utils.expand(ifc1);

      String ifc2 = GlobalIdGenerator.compress(uuid);
      String uuid2 = GlobalIdGenerator.expand(ifc2);

      String uuid12 = utils.expand(ifc2);
      String uuid21 = GlobalIdGenerator.expand(ifc1);

      System.out.println("Python: " + ifc1 + " " + uuid1 + " " + uuid12);
      System.out.println("Java:   " + ifc2 + " " + uuid2 + " " + uuid21);
    }

    long t0 = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++)
    {
      utils.compress(UUID.randomUUID().toString());
    }
    long el0 = System.currentTimeMillis() - t0;
    System.out.println("Time python: " + el0);

    t0 = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++)
    {
      GlobalIdGenerator.compress(UUID.randomUUID().toString());
    }
    long el1 = System.currentTimeMillis() - t0;
    System.out.println("Time Java: " + el1);

    System.out.println("Factor: " + ((double) el0 / (double) el1));

    // String a = "0ff7823a";
    // byte[] bb = GlobalIdGenerator.hexStringToByteArray(a);
    // String a2 = GlobalIdGenerator.byteArrayToHexString(bb);
    // System.out.println(a + " - " + a2);

  }
}
