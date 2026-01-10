# Lite UI Plan

## Foundations
- [x] ~~Add Lite UI flag to `UiSettings` and `UiSettingsFlow`.~~
- [x] ~~Add new string resources for Lite UI name + description (default `values/strings.xml`).~~
- [x] ~~Persist Lite UI selection in `UiSharedPreferences`.~~

## Settings Surface
- [x] ~~Add a Lite UI toggle or selector entry in `AppSettingsScreen`.~~
- [x] ~~Decide interaction with existing “UI style” selector (keep both or replace the selector with Lite + existing choices).~~
- [x] ~~Ensure switching to Lite does not change post style (“Simplified”) settings.~~

## Navigation & Entry Points
- [x] ~~Add `UiSettingsState.isLiteMode()` helper.~~
- [x] ~~Filter bottom navigation items for Lite (Home, Message, Video, Discover, Notification).~~
- [x] ~~Audit and gate drawer navigation rows (`DrawerContent`): Profile, Lists, Bookmarks, Drafts, Relays, Media Servers, Security, Privacy, Backup Keys dialog, App Settings, User Settings, Accounts switcher.~~
- [x] ~~Gate route availability in `AppNavigation` for heavy screens so deeplinks do not land in Lite-only-disabled UI.~~
- [x] ~~Gate route generation in `routeFor`/`routeReplyTo` paths when they resolve to heavy screens.~~
- [x] ~~Gate `uriToRoute` and QR/intent entry points to avoid launching heavy routes in Lite mode.~~

## Route Inventory (for gating)
- [ ] Core tabs: Home, Message, Video, Discover, Notification.
- [ ] Settings & management: Settings, UserSettings, EditRelays, EditMediaServers, SecurityFilters, PrivacyOptions, Nip47NWCSetup.
- [ ] Lists/bookmarks: Lists, MyPeopleListView, MyFollowPackView, PeopleListManagement, PeopleListMetadataEdit, FollowPackMetadataEdit, BookmarkGroups, BookmarkGroupView, BookmarkGroupMetadataEdit, PostBookmarkManagement, ArticleBookmarkManagement.
- [ ] Content/detail: Profile, Note, Hashtag, Geohash, Community, FollowPack, RelayInfo, ContentDiscovery.
- [ ] Chats/DMs: Room, RoomByAuthor, PublicChatChannel, LiveActivityChannel, EphemeralChat, NewEphemeralChat, NewGroupDM, ChannelMetadataEdit, NewPublicMessage.
- [ ] Composer flows: NewShortNote, GenericCommentPost, HashtagPost, GeoPost, NewProduct, VoiceReply.
- [ ] Misc: QRDisplay, EventRedirect, ManualZapSplitPayment, Search.

## Feed & Post UI
- [x] ~~Disable or simplify reactions/boost/repost UI elements in Lite mode.~~
- [x] ~~Disable media-heavy elements (image/video previews, rich cards) in feeds for Lite mode.~~
- [x] ~~Keep core actions (read, reply, repost) usable and visually intact.~~

## Secondary Screens
- [x] ~~Gate video-focused screens and components.~~
- [x] ~~Gate discover/trending or other heavy screens.~~
- [x] ~~Gate advanced composer add-ons if applicable (media uploads, embeds).~~

## QA & Documentation
- [ ] Verify setting persists across restart and applies immediately.
- [ ] Smoke-test navigation (no crashes when Lite hides tabs).
- [ ] Update `AGENTS.md` and/or docs if additional Lite UI guidance is needed.
