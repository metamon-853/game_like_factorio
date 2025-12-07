# game_like_factorio

[gdx-liftoff](https://github.com/libgdx/gdx-liftoff)で生成された[libGDX](https://libgdx.com/)プロジェクトです。

このプロジェクトは、シンプルなアプリケーションランチャーとlibGDXロゴを描画する`ApplicationAdapter`拡張を含むテンプレートで生成されました。

## Platforms

- `core`: すべてのプラットフォームで共有されるアプリケーションロジックを含むメインモジュール。
- `lwjgl3`: LWJGL3を使用する主要なデスクトッププラットフォーム（以前のドキュメントでは'desktop'と呼ばれていました）。

## Gradle

このプロジェクトは依存関係の管理に[Gradle](https://gradle.org/)を使用しています。
Gradleラッパーが含まれているため、`gradlew.bat`または`./gradlew`コマンドを使用してGradleタスクを実行できます。

便利なGradleタスクとフラグ：

- `--continue`: このフラグを使用すると、エラーが発生してもタスクの実行が停止しません。
- `--daemon`: このフラグにより、選択したタスクの実行にGradleデーモンが使用されます。
- `--offline`: このフラグを使用すると、キャッシュされた依存関係アーカイブが使用されます。
- `--refresh-dependencies`: このフラグは、すべての依存関係の検証を強制します。スナップショットバージョンに便利です。
- `build`: すべてのプロジェクトのソースとアーカイブをビルドします。
- `clean`: コンパイルされたクラスとビルドされたアーカイブを保存する`build`フォルダを削除します。
- `lwjgl3:jar`: アプリケーションの実行可能なjarをビルドします。`lwjgl3/build/libs`に配置されます。
- `lwjgl3:run`: アプリケーションを起動します。
- `test`: ユニットテストを実行します（存在する場合）。

単一のプロジェクトに固有でないほとんどのタスクは、`name:`プレフィックスを使用して実行できます。`name`は特定のプロジェクトのIDに置き換える必要があります。
例えば、`core:clean`は`core`プロジェクトの`build`フォルダのみを削除します。
