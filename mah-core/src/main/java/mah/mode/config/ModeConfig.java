/**
 * MIT License
 *
 * Copyright (c) 2017 zgqq
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package mah.mode.config;

import mah.keybind.config.KeybindConfig;

import java.util.HashSet;
import java.util.List;

/**
 * Created by zgq on 2017-01-09 13:44
 */
public class ModeConfig {

    private String parent;
    private String name;
    private ModeConfig parentMode;
    private List<KeybindConfig> keybinds;
    private HashSet<KeybindConfig> caches;

    public HashSet<KeybindConfig> getKeybinds() {
        if (caches != null) {
            return caches;
        }
        HashSet<KeybindConfig> allKeybinds = new HashSet<>();
        if (parentMode != null) {
            HashSet<KeybindConfig> keybinds = parentMode.getKeybinds();
            allKeybinds.addAll(keybinds);
        }
        allKeybinds.addAll(keybinds);
        caches = allKeybinds;
        return allKeybinds;
    }

    public void setKeybinds(List<KeybindConfig> keybinds) {
        this.keybinds = keybinds;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModeConfig getParentMode() {
        return parentMode;
    }

    public void setParentMode(ModeConfig parentMode) {
        this.parentMode = parentMode;
    }
}
