package com.graphicsfuzz;

import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.config.DefaultConfig;
import com.graphicsfuzz.scope.FuzzerScopeEntry;
import com.graphicsfuzz.scope.UnifiedTypeProxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProgramStateTest {

  ProgramState programState;
  FuzzerScopeEntry var0;
  FuzzerScopeEntry var1;
  FuzzerScopeEntry var2;
  FuzzerScopeEntry var3;
  FuzzerScopeEntry var4;
  List<String> names = Arrays.asList("var0", "var1", "var2", "var3", "var4");

  @Before
  public void setup() {
    programState = new ProgramState(new DefaultConfig());
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
  public void testGetReadEntriesOfCompatibleType() {
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
    programState.setIsInitializer(true);
    entries = programState.getReadEntriesOfCompatibleType(BasicType.INT);
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var0", "var2", "var4"));
    programState.setEntryHasBeenRead(var0);
    programState.setEntryHasBeenRead(var3);
    entries = programState.getReadEntriesOfCompatibleType(BasicType.INT);
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var0", "var2", "var4"));
    programState.setEntryHasBeenWritten(var4);
    entries = programState.getReadEntriesOfCompatibleType(BasicType.INT);
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var0", "var2"));
    programState.setIsInitializer(false);
    entries = programState.getReadEntriesOfCompatibleType(BasicType.INT);
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var0", "var2", "var4"));
  }

  @Test
  public void testGetWriteAvailableEntries() {
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
    programState.setIsInitializer(true);
    entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), names);
    programState.setEntryHasBeenRead(var0);
    programState.setEntryHasBeenRead(var3);
    entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var1", "var2", "var4"));
    programState.setEntryHasBeenWritten(var4);
    entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), Arrays.asList("var1", "var2"));
    programState.setIsInitializer(false);
    entries = programState.getWriteAvailableEntries();
    Assert.assertEquals(entries.stream().map(FuzzerScopeEntry::getName)
        .sorted().collect(Collectors.toList()), names);
  }
}
