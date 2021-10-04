package com.graphicsfuzz;

import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.config.ParameterConfiguration;
import com.graphicsfuzz.scope.FuzzerScopeEntry;
import com.graphicsfuzz.scope.UnifiedTypeProxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProgramStateTest {

  //TODO write tests for readonly writeonly const variables
  //TODO write tests for complex settings with enclosed iniitalizers and function calls
  ProgramState programState;
  FuzzerScopeEntry var0;
  FuzzerScopeEntry var1;
  FuzzerScopeEntry var2;
  FuzzerScopeEntry var3;
  FuzzerScopeEntry var4;
  List<String> names = Arrays.asList("var0", "var1", "var2", "var3", "var4");

  @Before
  public void setup() {
    programState = new ProgramState(new ParameterConfiguration.Builder().getConfig());
    programState.addVariable("var0", new UnifiedTypeProxy(BasicType.INT));
    programState.addVariable("var1", new UnifiedTypeProxy(BasicType.UINT));
    programState.addVariable("var2", new UnifiedTypeProxy(BasicType.IVEC2));
    programState.addVariable("var3", new UnifiedTypeProxy(BasicType.UVEC3));
    programState.addVariable("var4", new UnifiedTypeProxy(BasicType.IVEC4));
    var0 = programState.getScopeEntryByName("var0");
    var1 = programState.getScopeEntryByName("var1");
    var2 = programState.getScopeEntryByName("var2");
    var3 = programState.getScopeEntryByName("var3");
    var4 = programState.getScopeEntryByName("var4");
  }

  @Test
  public void testGetReadEntriesOfCompatibleTypeWithInitializer() {
    List<FuzzerScopeEntry> entries = programState.getReadEntriesOfCompatibleType(BasicType.UINT);
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var1", "var3"));
    programState.setEntryHasBeenRead(var1);
    entries = programState.getReadEntriesOfCompatibleType(BasicType.UINT);
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var1", "var3"));
    programState.setEntryHasBeenRead(var3);
    programState.setEntryHasBeenWritten(var1);
    entries = programState.getReadEntriesOfCompatibleType(BasicType.UINT);
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var1", "var3"));

    // Enter initializer
    programState.enterInitializer();
    entries = programState.getReadEntriesOfCompatibleType(BasicType.INT);
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var0", "var2", "var4"));
    programState.setEntryHasBeenRead(var0);
    programState.setEntryHasBeenRead(var3);

    // Enter initializer second param
    programState.finishInitParam();
    entries = programState.getReadEntriesOfCompatibleType(BasicType.INT);
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var0", "var2", "var4"));
    programState.setEntryHasBeenWritten(var4);

    // Enter initializer third param
    programState.finishInitParam();
    entries = programState.getReadEntriesOfCompatibleType(BasicType.INT);
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var0", "var2"));
    programState.exitInitializer();
    entries = programState.getReadEntriesOfCompatibleType(BasicType.INT);
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var0", "var2", "var4"));
  }

  @Test
  public void testGetWriteAvailableEntriesWithInitializer() {
    // No initializer
    List<FuzzerScopeEntry> entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), names);
    programState.setEntryHasBeenRead(var1);
    entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), names);
    programState.setEntryHasBeenRead(var3);
    programState.setEntryHasBeenWritten(var1);
    entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), names);

    // Enter initializer first param
    programState.enterInitializer();
    entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), names);
    programState.setEntryHasBeenRead(var0);
    programState.setEntryHasBeenRead(var3);
    entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), names);

    // Enter initializer second param
    programState.finishInitParam();
    entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var1", "var2", "var4"));
    programState.setEntryHasBeenWritten(var4);

    // Enter initializer third param
    programState.finishInitParam();
    entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var1", "var2"));
    programState.exitInitializer();


    entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), names);
  }

}
