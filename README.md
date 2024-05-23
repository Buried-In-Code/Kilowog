# Kilowog

![Java Version](https://img.shields.io/badge/Temurin-17-green?style=flat-square&logo=eclipse-adoptium)
![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9.22-green?style=flat-square&logo=kotlin)
![Status](https://img.shields.io/badge/Status-Beta-yellowgreen?style=flat-square)

[![Gradle](https://img.shields.io/badge/Gradle-8.6-informational?style=flat-square&logo=gradle)](https://github.com/gradle/gradle)
[![Ktlint](https://img.shields.io/badge/Ktlint-1.2.1-informational?style=flat-square)](https://github.com/pinterest/ktlint)

[![Github - Version](https://img.shields.io/github/v/tag/ComicCorps/Kilowog?logo=Github&label=Version&style=flat-square)](https://github.com/ComicCorps/Kilowog/tags)
[![Github - License](https://img.shields.io/github/license/ComicCorps/Kilowog?logo=Github&label=License&style=flat-square)](https://opensource.org/licenses/MIT)
[![Github - Contributors](https://img.shields.io/github/contributors/ComicCorps/Kilowog?logo=Github&label=Contributors&style=flat-square)](https://github.com/ComicCorps/Kilowog/graphs/contributors)

Kilowog's goal is to help sort and organize your comic collection by using the information stored in Info files.\
Kilowog will format all you digital comics into a singular format (cbz).\
It adds and updates the info files using the supported services.\
Unlike other tagging tools, Kilowog takes a manual approach when no Info file is provided, requiring you to enter the Publisher/Series/Issue details needed to search. 

## Execution

```bash
./gradlew clean run
```

To force processing run with the flag: `--args="force"`

## Supported Formats

### File formats

- .cbz
- .cbr

### Info files

- [Metadata.xml](https://github.com/ComicCorps/Schemas)
- [MetronInfo.xml](https://github.com/Metron-Project/metroninfo)
- [ComicInfo.xml](https://github.com/anansi-project/comicinfo)

## Services

- [Comicvine](https://comicvine.gamespot.com)
- _WIP [League of Comic Geeks](https://leagueofcomicgeeks.com)_
- _WIP [Marvel](https://marvel.com/comics)_
- [Metron](https://metron.cloud)

## Socials

[![Social - Mastodon](https://img.shields.io/badge/%40ComicCorps-teal?label=Mastodon&logo=mastodon&style=for-the-badge)](https://mastodon.social/@ComicCorps)\
[![Social - Matrix](https://img.shields.io/badge/%23ComicCorps-teal?label=Matrix&logo=matrix&style=for-the-badge)](https://matrix.to/#/#ComicCorps:matrix.org)
