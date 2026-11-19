# Contract: UI Navigation (Phase 2)

## Sidebar entries

Ordered: CPU, RAM, Disk, Network, GPU, Processes.

Selecting an entry shows the corresponding detail pane (or process table). Active entry uses green accent style class `sidebar-item-active`.

## Detail panes

- **CPU**: aggregate %, history chart, per-core list/bars
- **RAM**: % + history
- **Disk**: aggregate %, history, per-volume list
- **Network**: in/out rates, history, interface list
- **GPU**: metrics or localized unavailable message
- **Processes**: table + search + sort + End Process

## Themes

AtlantaFX base + `houdini-accent.css` loaded for light and dark.
