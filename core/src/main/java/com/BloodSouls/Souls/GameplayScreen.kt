package com.BloodSouls.Souls

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.viewport.ScreenViewport

enum class ActionState { NONE, ATTACK, BLOCK_START, BLOCK_IDLE, JUMP, LOOK_AROUND }

class GameplayScreen(private val game: BloodSoulsGame) : Screen {

    companion object {
        private const val TAG = "GameplayScreen"
    }

    private val camera = OrthographicCamera()
    private var map: TiledMap? = null
    private var mapRenderer: OrthogonalTiledMapRenderer? = null

    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()
    private val stage = Stage(ScreenViewport())
    private val joystick: Joystick
    private val buttons = Buttons()

    private val knightAnims = KnightAnimations()
    private var currentAnim: Animation<TextureRegion>? = null
    private var stateTimer = 0f
    private var playerPosition = Vector2(800f, 800f)

    private var playerHp = 100f
    private val maxHp = 100f
    private var hpLabel: Label? = null

    private var facingDirection = Direction.S
    private var actionState = ActionState.NONE

    private val fallbackTexture: Texture by lazy {
        val pm = Pixmap(64, 64, Pixmap.Format.RGBA8888)
        pm.setColor(Color.FIREBRICK)
        pm.fillCircle(32, 32, 28)
        pm.setColor(Color.GOLD)
        pm.drawCircle(32, 32, 28)
        val tex = Texture(pm)
        pm.dispose()
        tex
    }

    init {
        Gdx.app.log(TAG, "Initializing GameplayScreen...")
        camera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        Gdx.input.inputProcessor = stage

        val joystickX = Gdx.graphics.width - 250f
        joystick = Joystick(stage, joystickX, 50f, 200f)
        buttons.addToStage(stage)

        try {
            val label = Label("HP: 100 / 100", SkinFactory.skin)
            label.setPosition(30f, Gdx.graphics.height - 40f)
            label.setFontScale(1.3f)
            stage.addActor(label)
            hpLabel = label
        } catch (e: Exception) {
            Gdx.app.error(TAG, "Label setup error: ${e.message}")
        }

        try {
            val loadedMap = TmxMapLoader().load("sewer_map.tmx")
            map = loadedMap
            mapRenderer = OrthogonalTiledMapRenderer(loadedMap, 6.5f)
            Gdx.app.log(TAG, "Loaded map 'sewer_map.tmx' successfully.")
        } catch (e: Exception) {
            Gdx.app.error(TAG, "Failed to load sewer_map.tmx: ${e.message}", e)
            createFallbackMap()
        }

        try {
            knightAnims.loadIdleAnimations("")
            knightAnims.loadRunAnimations("")
            knightAnims.loadAttackAnimations("")
            knightAnims.loadBlockAnimations("")
            currentAnim = knightAnims.getIdleAnimation(KnightIdle.NORMAL, facingDirection)
        } catch (e: Exception) {
            Gdx.app.error(TAG, "Failed to load knight animations: ${e.message}", e)
        }

        buttons.onAttack = {
            val anim = knightAnims.playAttack(1, facingDirection)
            if (anim != null) {
                currentAnim = anim
                stateTimer = 0f
                actionState = ActionState.ATTACK
            }
        }
        buttons.onBlock = {
            val anim = knightAnims.playBlockStart(facingDirection)
            if (anim != null) {
                currentAnim = anim
                stateTimer = 0f
                actionState = ActionState.BLOCK_START
            }
        }
        buttons.onJump = {
            val anim = knightAnims.getIdleAnimation(KnightIdle.JUMP, facingDirection)
            if (anim != null) {
                currentAnim = anim
                stateTimer = 0f
                actionState = ActionState.JUMP
            }
        }
        buttons.onLookAround = {
            val anim = knightAnims.getIdleAnimation(KnightIdle.LOOK_AROUND, facingDirection)
            if (anim != null) {
                currentAnim = anim
                stateTimer = 0f
                actionState = ActionState.LOOK_AROUND
            }
        }
    }

    private fun createFallbackMap() {
        try {
            val fallbackMap = TiledMap()
            val layer = TiledMapTileLayer(20, 20, 32, 32)

            val pixmap = Pixmap(32, 32, Pixmap.Format.RGBA8888)
            pixmap.setColor(Color.DARK_GRAY)
            pixmap.fill()
            pixmap.setColor(Color.GRAY)
            pixmap.drawRectangle(0, 0, 32, 32)
            val floorTexture = Texture(pixmap)
            pixmap.dispose()

            val tile = StaticTiledMapTile(TextureRegion(floorTexture))
            tile.id = 1

            for (x in 0 until 20) {
                for (y in 0 until 20) {
                    val cell = TiledMapTileLayer.Cell()
                    cell.tile = tile
                    layer.setCell(x, y, cell)
                }
            }

            fallbackMap.layers.add(layer)
            map = fallbackMap
            mapRenderer = OrthogonalTiledMapRenderer(fallbackMap, 6.5f)
            Gdx.app.log(TAG, "Created 20x20 fallback procedural map successfully.")
        } catch (e: Exception) {
            Gdx.app.error(TAG, "Fallback map creation error: ${e.message}")
        }
    }

    private fun getTileIdAtWorldPos(x: Float, y: Float): Long {
        val activeMap = map ?: return 0L
        val layer = activeMap.layers.get(0) as? TiledMapTileLayer ?: return 0L
        val tileSizeWorld = 32f * 6.5f
        val tileX = (x / tileSizeWorld).toInt()
        val tileY = (y / tileSizeWorld).toInt()

        if (tileX !in 0 until layer.width || tileY !in 0 until layer.height) {
            return 17L
        }

        val cell = layer.getCell(tileX, tileY) ?: return 0L
        return cell.tile?.id?.toLong() ?: 0L
    }

    private fun isLavaTile(tileId: Long): Boolean {
        if (tileId == 0L) return false
        val masked = tileId and 0x1FFFFFFF
        return tileId == 86L || masked == 86L
    }

    private fun isWalkableTile(tileId: Long): Boolean {
        if (tileId == 0L) return true

        val collisionIds = setOf(
            17L, 28L, 29L, 1610612754L, 2684354577L, 3221225489L, 2684354598L, 2684354599L
        )

        val masked = tileId and 0x1FFFFFFF
        if (collisionIds.contains(tileId) || collisionIds.contains(masked)) {
            return false
        }

        return true
    }

    private fun isPositionWalkable(x: Float, y: Float, width: Float, height: Float): Boolean {
        val footCenterX = x + width / 2f
        val footCenterY = y + 15f

        val tileId = getTileIdAtWorldPos(footCenterX, footCenterY)
        return isWalkableTile(tileId)
    }

    private fun checkLavaDamage(x: Float, y: Float, width: Float, height: Float, delta: Float) {
        val footCenterX = x + width / 2f
        val footCenterY = y + 15f
        val tileId = getTileIdAtWorldPos(footCenterX, footCenterY)

        if (isLavaTile(tileId)) {
            playerHp = (playerHp - 25f * delta).coerceAtLeast(0f)
        } else {
            if (playerHp < maxHp) {
                playerHp = (playerHp + 2f * delta).coerceAtMost(maxHp)
            }
        }

        hpLabel?.setText("HP: ${playerHp.toInt()} / ${maxHp.toInt()}")
        if (isLavaTile(tileId)) {
            hpLabel?.color = Color.RED
        } else {
            hpLabel?.color = Color.GREEN
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stateTimer += delta

        val frame = currentAnim?.getKeyFrame(stateTimer)
        val frameWidth = frame?.regionWidth?.toFloat() ?: 64f
        val frameHeight = frame?.regionHeight?.toFloat() ?: 64f

        val mapTileWidth = map?.properties?.get("width", Int::class.java) ?: 20
        val mapTileHeight = map?.properties?.get("height", Int::class.java) ?: 20
        val tilePixelWidth = map?.properties?.get("tilewidth", Int::class.java) ?: 32
        val tilePixelHeight = map?.properties?.get("tileheight", Int::class.java) ?: 32

        val mapWorldWidth = mapTileWidth * tilePixelWidth * 6.5f
        val mapWorldHeight = mapTileHeight * tilePixelHeight * 6.5f

        val joyVec = joystick.getDirectionVector()
        if (joyVec.len() > 0.1f) {
            facingDirection = when {
                joyVec.x > 0.5f && joyVec.y > 0.5f -> Direction.NE
                joyVec.x > 0.5f && joyVec.y < -0.5f -> Direction.SE
                joyVec.x < -0.5f && joyVec.y > 0.5f -> Direction.NW
                joyVec.x < -0.5f && joyVec.y < -0.5f -> Direction.SW
                joyVec.x > 0.5f -> Direction.E
                joyVec.x < -0.5f -> Direction.W
                joyVec.y > 0.5f -> Direction.N
                else -> Direction.S
            }

            val animFinished = currentAnim?.isAnimationFinished(stateTimer) ?: true
            if (actionState == ActionState.NONE || currentAnim == null || animFinished) {
                actionState = ActionState.NONE
                currentAnim = knightAnims.getRunAnimation(facingDirection)
            }

            val speed = 250f * delta
            val dir = Vector2(joyVec).nor()
            val deltaX = dir.x * speed
            val deltaY = dir.y * speed

            val nextX = playerPosition.x + deltaX
            if (isPositionWalkable(nextX, playerPosition.y, frameWidth, frameHeight)) {
                playerPosition.x = nextX
            }

            val nextY = playerPosition.y + deltaY
            if (isPositionWalkable(playerPosition.x, nextY, frameWidth, frameHeight)) {
                playerPosition.y = nextY
            }
        } else {
            if (actionState != ActionState.NONE && currentAnim != null) {
                if (currentAnim!!.isAnimationFinished(stateTimer)) {
                    when (actionState) {
                        ActionState.BLOCK_START -> {
                            val blockIdleAnim = knightAnims.playBlockIdle(facingDirection)
                            if (blockIdleAnim != null) {
                                currentAnim = blockIdleAnim
                                stateTimer = 0f
                                actionState = ActionState.BLOCK_IDLE
                            } else {
                                actionState = ActionState.NONE
                            }
                        }
                        ActionState.ATTACK, ActionState.JUMP, ActionState.LOOK_AROUND -> {
                            actionState = ActionState.NONE
                        }
                        ActionState.BLOCK_IDLE -> {
                        }
                        else -> actionState = ActionState.NONE
                    }
                }
            }

            if (actionState == ActionState.NONE) {
                currentAnim = knightAnims.getIdleAnimation(KnightIdle.NORMAL, facingDirection)
            }
        }

        checkLavaDamage(playerPosition.x, playerPosition.y, frameWidth, frameHeight, delta)

        playerPosition.x = playerPosition.x.coerceIn(0f, mapWorldWidth - frameWidth)
        playerPosition.y = playerPosition.y.coerceIn(0f, mapWorldHeight - frameHeight)

        val playerCenterX = playerPosition.x + frameWidth / 2f
        val playerCenterY = playerPosition.y + frameHeight / 2f

        if (mapWorldWidth > camera.viewportWidth) {
            camera.position.x = playerCenterX.coerceIn(
                camera.viewportWidth / 2f,
                mapWorldWidth - camera.viewportWidth / 2f
            )
        } else {
            camera.position.x = mapWorldWidth / 2f
        }

        if (mapWorldHeight > camera.viewportHeight) {
            camera.position.y = playerCenterY.coerceIn(
                camera.viewportHeight / 2f,
                mapWorldHeight - camera.viewportHeight / 2f
            )
        } else {
            camera.position.y = mapWorldHeight / 2f
        }

        camera.update()

        try {
            mapRenderer?.setView(camera)
            mapRenderer?.render()
        } catch (e: Exception) {
            Gdx.app.error(TAG, "Map render error: ${e.message}")
        }

        batch.projectionMatrix = camera.combined
        batch.begin()
        if (frame != null) {
            batch.draw(frame, playerPosition.x, playerPosition.y)
        } else {
            batch.draw(fallbackTexture, playerPosition.x, playerPosition.y)
        }
        batch.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(30f, Gdx.graphics.height - 70f, 200f, 20f)

        val healthRatio = (playerHp / maxHp).coerceIn(0f, 1f)
        shapeRenderer.color = if (healthRatio < 0.3f) Color.RED else Color.GREEN
        shapeRenderer.rect(30f, Gdx.graphics.height - 70f, 200f * healthRatio, 20f)
        shapeRenderer.end()

        try {
            stage.act(delta)
            stage.draw()
        } catch (e: Exception) {
            Gdx.app.error(TAG, "Stage render error: ${e.message}")
        }
    }

    override fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()

        stage.viewport.update(width, height, true)
        val joystickX = stage.viewport.worldWidth - 250f
        joystick.setPosition(joystickX, 50f)
        buttons.updatePositions(50f, 50f)
        hpLabel?.setPosition(30f, stage.viewport.worldHeight - 40f)
    }

    override fun show() {
        Gdx.app.log(TAG, "show called: GameplayScreen active")
    }

    override fun hide() {
        Gdx.app.log(TAG, "hide called")
    }

    override fun pause() {
        Gdx.app.log(TAG, "pause called")
    }

    override fun resume() {
        Gdx.app.log(TAG, "resume called")
    }

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
        stage.dispose()
        map?.dispose()
        mapRenderer?.dispose()
        joystick.dispose()
        buttons.dispose()
        knightAnims.dispose()
    }
}
