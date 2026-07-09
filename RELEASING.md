# リリース手順

このリポジトリは `com.ponkotuy % flyway-play` として Maven Central(Sonatype Central Portal)に公開している。

## 手順

1. `build.sbt` の `flywayPlayVersion` を上げる
   - Maven Central は同一バージョンの再公開・削除が一切できないので、必ず新しい番号にする
2. main に push して Scala CI が通ることを確認
3. GitHub の Actions タブ → **Release** ワークフロー → **Run workflow**(main)を実行
   - 中身は `sbt +publishSigned sonatypeBundleRelease`。Scala 2.13 / 3 の両方が公開される
4. 完了後、数分〜数十分で依存解決が可能になる
   ```scala
   libraryDependencies += "com.ponkotuy" %% "flyway-play" % "<version>"
   ```

## トラブルシュート

### ジョブが赤×でも公開に成功していることがある

バンドルのアップロード後、sbt-sonatype がステータス確認 API をポーリングする。この呼び出しが
一時的にタイムアウトすると `STATUS_CHECK_FAILURE` でジョブは失敗扱いになるが、
Central 側では公開処理が進行している(10.0.0 リリース時に実際に発生)。

**再実行する前に**必ず実状態を確認すること:

- central.sonatype.com にログインして Deployments 画面を見る、または
- status API を叩く(ログに出る deployment id を使う):
  ```
  POST https://central.sonatype.com/api/v1/publisher/status?id=<deployment id>
  Authorization: Bearer <base64(tokenUser:tokenPassword)>
  ```

既に `PUBLISHING` / `PUBLISHED` なら再実行してはいけない(同一バージョンの二重公開はエラーになる)。

### SNAPSHOT エラー

`Version cannot be a snapshot version` が出た場合、ルートプロジェクトのバージョンが
SNAPSHOT になっている。`ThisBuild / version := flywayPlayVersion` で全体を揃えているので、
この設定を消さないこと。

## セットアップ(新しい環境・鍵やトークンの更新時)

### GitHub Secrets(Actions からのリリースに必要)

| Secret | 内容 |
|---|---|
| `SONATYPE_USERNAME` / `SONATYPE_PASSWORD` | central.sonatype.com の User Token(アカウント → Generate User Token) |
| `PGP_SECRET` | 署名用 GPG 秘密鍵(`gpg --export-secret-keys --armor <KEY_ID>`) |

- namespace `com.ponkotuy` は旧 OSSRH 時代(2016)のアカウントに紐付いている。トークンはそのアカウントで発行する
- 署名鍵(パスフレーズなし): `3F2C89F0CA77EB36EAD3649445170F5C5E71E273`(keyserver.ubuntu.com に公開済み)。
  鍵を作り直したら keyserver への送信と `PGP_SECRET` の更新を忘れずに

### ローカルから publish する場合

`~/.sbt/1.0/sonatype.sbt` を作成:

```scala
credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "central.sonatype.com",
  "<トークンのusername>",
  "<トークンのpassword>"
)
```

GPG 秘密鍵をインポートした上で `sbt +publishSigned sonatypeBundleRelease` を実行する。
