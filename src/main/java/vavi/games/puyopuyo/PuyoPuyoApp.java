/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.games.puyopuyo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.tv.xlet.Xlet;
import javax.tv.xlet.XletContext;

import org.havi.ui.HScene;
import org.havi.ui.HSceneFactory;


/**
 * PuyoPuyoApp
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 090106 nsano initial version <br>
 */
public class PuyoPuyoApp implements Xlet {

    /** */
    private HScene scene;
    /** */
    private MyView gui;
    /** */
    private XletContext context;

    /* */
    public void initXlet(XletContext context) {
        this.context = context;

        this.scene = HSceneFactory.getInstance().getDefaultHScene();

        try {
            this.gui = new MyView();

            gui.setSize(640, 400);
            scene.add(gui, BorderLayout.CENTER);
        } catch (Exception e) {
e.printStackTrace(System.err);
        }

        scene.validate();
    }

    private Thread thread;

    /* */
    public void startXlet() {
        gui.setVisible(true);
        scene.setVisible(true);
        gui.requestFocus();

        thread = new Thread(gui);
        thread.start();
    }

    /* */
    public void pauseXlet() {
        gui.setVisible(false);
    }

    /* */
    public void destroyXlet(boolean unconditional) {
        thread.interrupt();
        gui.stop();

        scene.remove(gui);
        scene = null;
    }

    /** */
    private class MyView extends Container implements PuyoPuyo.View, Runnable {

        /** */
        private PuyoPuyo.Stage stage;

        /** */
        private Image offscreenImage;
        /** */
        private Graphics ofscreenGraphics;
        /** */
        private Image wallImage;
        /** */
        private Image fieldImage;
        /** */
        private Image nextImage;
        /** */
        private Image[] images;

        /** */
        private int offScreenWidth;
        /** */
        private int offScreenHeight;
        /** */
//        private MediaPlayer[] clips;

        /** */
        private int[] fieldLefts, fieldTops;
        /** �Ղ�̕� */
        private int puyoSize;

        /** �Q�[�����X�^�[�g���������� */
        private int startFlag;
        /** �ꎞ��~�E�X�^�[�g���� */
        private int stopFlag;

        private String state = ""; // "test";

        /** ���y�̒�~ */
        public void stopClips() {
//            for (int i = 0; i < clips.length; i++) {
//                clips[i].stop();
//            }
        }

        /** */
        MyView() {

            addKeyListener(keyListener);

            // �p�����[�^���擾
            String[] args = (String[]) context.getXletProperty(XletContext.ARGS);
            args = new String[] { "2", "456", "272", "0", "1", "2" };

            int playersCount = Integer.parseInt(args[0]); // n
            stage = new PuyoPuyo.Stage(playersCount);
            offScreenWidth = Integer.parseInt(args[1]); // w
            offScreenHeight = Integer.parseInt(args[2]); // h
            stage.set = Integer.parseInt(args[3]); // s
            stage.soundFlag = Integer.parseInt(args[4]); // v
            stage.puyoFlag = Integer.parseInt(args[5]); // p

            // ���y�t�@�C���ǂݍ���
//          clips = new MediaPlayer[6];
//          clips[0] = MediaPlayer.create(context, R.raw.puyo_08); // BGM
//          clips[1] = MediaPlayer.create(context, R.raw.a728); // �I��
//          clips[2] = MediaPlayer.create(context, R.raw.pyoro22); // �ړ�
//          clips[3] = MediaPlayer.create(context, R.raw.puu58); // ��]
//          clips[4] = MediaPlayer.create(context, R.raw.puu47); // �ςݏグ
//          clips[5] = MediaPlayer.create(context, R.raw.open23); // �������

            // �摜�ǂݍ���
            MediaTracker mt = new MediaTracker(this);
            wallImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/wall.gif"));
            mt.addImage(wallImage, 0);
            fieldImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/dt13.gif"));
            mt.addImage(fieldImage, 1);
            nextImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/next.gif"));
            mt.addImage(nextImage, 2);
            images = new Image[12];
            images[0] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/back.gif"));
            mt.addImage(images[0], 3);
            images[1] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/gray.gif"));
            mt.addImage(images[1], 4);
            images[2] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/red.gif"));
            mt.addImage(images[2], 5);
            images[3] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/yellow.gif"));
            mt.addImage(images[3], 6);
            images[4] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/blue.gif"));
            mt.addImage(images[4], 7);
            images[5] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/green.gif"));
            mt.addImage(images[5], 8);
            images[6] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/purple.gif"));
            mt.addImage(images[6], 9);
            try { mt.waitForAll(); } catch (InterruptedException e) {}

            // ������
            stage.init();
            puyoSize = 16;
            startFlag = 0;
            stopFlag = 0;
        }

        /** */
        private KeyListener keyListener = new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                int keyCode = keyEvent.getKeyCode();
                if (keyCode == KeyEvent.VK_R) {
                    // ���Z�b�g
                    stopClips();
                    stage.init();
                    puyoSize = 16;
                    startFlag = 0;
                    stopFlag = 0;
                    for (int i = 0; i < stage.playersCount; i++) {
                        stage.games[i].init();
                    }
                    repaint();
                } else if (startFlag == 0 && keyCode == KeyEvent.VK_S) {
                    // �X�^�[�g
                    startFlag = 1;
                    if (stage.soundFlag == 1) {
//                        clips[0].setLooping(true);
//                        clips[0].start();
                    }
                    for (int i = 0; i < stage.playersCount; i++) {
                        stage.games[i].start();
                    }
                } else if (startFlag == 1 && stage.games[0].waitFlag == 0) {
                    // �Q�[�����X�^�[�g���Ă�����

                    // �X�g�b�v
                    if (keyCode == KeyEvent.VK_S && stage.playersCount == 1) {
                        if (stopFlag == 0) {
                            stage.games[0].waitFlag = 1;
                            stage.games[0].sleep(PuyoPuyo.FallSpeed, "Stop");
                        } else {
                            stage.games[0].autoFall();
                        }
                        // �Q�[���ĊJ���ꎞ��~
                        if (stopFlag == 1) {
                            stopFlag = 0;
                        } else {
                            stopFlag = 1;
                        }
                        // �`��
                        repaint();
                    }
                    // �ړ��E��]�E�I�[�g���[�h
                    if (stopFlag == 0 && stage.games[0].waitFlag == 0) {
                        if (keyCode == KeyEvent.VK_X) { // ��]
                            stage.games[0].rotate(1);
                        } else if (keyCode == KeyEvent.VK_Z) { // ��]
                            stage.games[0].rotate(2);
                        } else if (keyCode == KeyEvent.VK_LEFT) { // ���ړ�
                            stage.games[0].left();
                        } else if (keyCode == KeyEvent.VK_RIGHT) { // �E�ړ�
                            stage.games[0].right();
                        } else if (keyCode == KeyEvent.VK_DOWN) { // ���ړ�
                            stage.games[0].down();
                        } else if (keyCode == KeyEvent.VK_UP) { // ��ړ�
                            stage.games[0].up();
                        } else if (keyCode == KeyEvent.VK_SPACE) { // ��C�ɉ��ړ�
                            stage.games[0].bottom();
                        } else if (keyCode == KeyEvent.VK_A) { // �I�[�g���[�h�؂�ւ�
                            if (stage.games[0].autoFlag == 0) {
                                stage.games[0].autoFlag = 1;
                                stage.games[0].autoMove();
                            } else {
                                stage.games[0].autoFlag = 0;
                            }
                            repaint();
                        } else if (keyCode == KeyEvent.VK_R) { // repaint
                            repaint();
                        }
                    }
                }
            }
        };

        /** */
        private boolean notified;

        /* */
        public void addNotify() {
            super.addNotify();
            notified = true;
        }

        /* */
        public void run() {
            while (!notified) {
                Thread.yield();
            }

            // ���z��ʂ��`
            offscreenImage = createImage(offScreenWidth, offScreenHeight);
            ofscreenGraphics = offscreenImage.getGraphics();

            // �t�H�[�J�X�����킹��
            this.requestFocus();
            // �I�u�W�F�N�g���`
            fieldLefts = new int[stage.playersCount];
            fieldTops = new int[stage.playersCount];
            for (int i = 0; i < stage.playersCount; i++) {
                stage.games[i] = new PuyoPuyo(stage, i);
                stage.games[i].setView(this);
                // �t�B�[���h�J�n�ʒu
                fieldLefts[i] = (i % 4) * ((stage.columns + 2) * puyoSize + 100);
                fieldTops[i] = (i - i % 4) / 4 * (stage.lows * puyoSize + 44);
            }
        }

        /* */
        void stop() {
            for (int i = 0; i < stage.playersCount; i++) {
                stage.gameFlags[i] = 0;
            }
            stopClips();
        }

        /* */
        public void paint(Graphics g) {

            // �t�B�[���h��\��
            for (int n = 0; n < stage.playersCount; n++) {
                // �w�i�摜
                ofscreenGraphics.drawImage(fieldImage, fieldLefts[n], fieldTops[n], null);
                // ��
                for (int i = 2; i < stage.lows; i++) {
                    ofscreenGraphics.drawImage(wallImage, fieldLefts[n], i * puyoSize + fieldTops[n], null);
                    ofscreenGraphics.drawImage(wallImage, fieldLefts[n] + (stage.columns + 1) * puyoSize, i * puyoSize + fieldTops[n], null);
                }
                for (int j = 0; j < stage.columns + 2; j++) {
                    ofscreenGraphics.drawImage(wallImage, fieldLefts[n] + j * puyoSize, stage.lows * puyoSize + fieldTops[n], null);
                }
                // �Ղ�
                for (int i = 2; i < stage.lows; i++) {
                    for (int j = 0; j < stage.columns; j++) {
                        ofscreenGraphics.drawImage(images[0], puyoSize + j * puyoSize + fieldLefts[n], i * puyoSize + fieldTops[n], null);
                        ofscreenGraphics.drawImage(images[stage.games[n].grid[i][j]], puyoSize + j * puyoSize + fieldLefts[n], i * puyoSize + fieldTops[n], null);
                    }
                }
                // �\���������
                ofscreenGraphics.drawImage(images[1], puyoSize + fieldLefts[n], puyoSize + fieldTops[n], null);
                // ����
                g.setColor(Color.black);
                ofscreenGraphics.drawString("Next", 137 + fieldLefts[n], 41 + fieldTops[n]); // NEXT
                ofscreenGraphics.drawString("" + stage.disturbCounts[n], 38 + fieldLefts[n], 30 + fieldTops[n]); // �������
                ofscreenGraphics.drawString(stage.games[n].overMessage, 135 + fieldLefts[n], 130 + fieldTops[n]); // �Q�[���I�[�o�[
                if (stopFlag == 1) { // Stop
                    ofscreenGraphics.drawString("STOP", 135 + fieldLefts[n], 190 + fieldTops[n]);
                }
                if (state != "test") {
                    ofscreenGraphics.drawString("Score: " + stage.games[n].score, 135 + fieldLefts[n], 170 + fieldTops[n]); // Score
                    if (stage.games[n].autoFlag == 1) { // Auto
                        ofscreenGraphics.drawString("AUTO", 135 + fieldLefts[n], 210 + fieldTops[n]);
                    }
                }
                // �A��
                if (stage.games[n].chainCount >= 1) {
                    ofscreenGraphics.drawString(stage.games[n].message + "�I", 135 + fieldLefts[n], 130 + fieldTops[n]);
                    ofscreenGraphics.drawString("(" + stage.games[n].chainCount + "�A��)", 135 + fieldLefts[n], 145 + fieldTops[n]);
                }
                // ���Ղ�
                if ((stage.games[n].waitFlag == 0 || stopFlag == 1) && stage.gameFlags[n] == 1) {
                    if (stage.games[n].pos[0][0] > 1) {
                        ofscreenGraphics.drawImage(images[stage.games[n].puyo1], (stage.games[n].pos[0][1] + 1) * puyoSize + fieldLefts[n], (stage.games[n].pos[0][0]) * puyoSize + fieldTops[n], null);
                    }
                    if (stage.games[n].pos[1][0] > 1) {
                        ofscreenGraphics.drawImage(images[stage.games[n].puyo2], (stage.games[n].pos[1][1] + 1) * puyoSize + fieldLefts[n], (stage.games[n].pos[1][0]) * puyoSize + fieldTops[n], null);
                    }
                }
                // NEXT�Ղ�
                ofscreenGraphics.drawImage(nextImage, 138 + fieldLefts[n], 47 + fieldTops[n], null);
                ofscreenGraphics.drawImage(images[stage.games[n].npuyo1], 143 + fieldLefts[n], 51 + fieldTops[n], null);
                ofscreenGraphics.drawImage(images[stage.games[n].npuyo2], 143 + fieldLefts[n], 67 + fieldTops[n], null);
                ofscreenGraphics.drawImage(images[stage.games[n].nnpuyo1], 159 + fieldLefts[n], 59 + fieldTops[n], null);
                ofscreenGraphics.drawImage(images[stage.games[n].nnpuyo2], 159 + fieldLefts[n], 75 + fieldTops[n], null);
                // �e�X�g�p
                if (state.equals("test")) {
                    g.setColor(Color.black);
                    // ����
                    ofscreenGraphics.drawString(stage.games[n].x, 10 + fieldLefts[n], 260 + fieldTops[n]); // "Chain=" + G[n].max_chain_num + ", " +
                    // �z��
                    for (int i = 0; i < stage.lows; i++) {
                        for (int j = 0; j < stage.columns; j++) {
                            if (stage.games[n].lastIgnitionLabel2[i][j] > 1) {
                                g.setColor(Color.red);
                            } else if (stage.games[n].lastIgnitionLabel2[i][j] == 1) {
                                g.setColor(Color.blue);
                            } else {
                                g.setColor(Color.black);
                            }
                            ofscreenGraphics.drawString("" + stage.games[n].lastChainLabel[i][j], 11 * j + 150 + fieldLefts[n], 11 * i + 100 + fieldTops[n]);
                        }
                    }
                }
            }
            // �ꊇ�\��
            g.drawImage(offscreenImage, 0, 0, null);
        }

        /* */
        public void play(int folder, int cn) {
//            play(getDocumentBase(), "sound/chain/" + folder + "/" + cn + ".au");
        }

        /* */
        public void playClip(int i) {
//            clips[i].start();
        }
    }
}

/* */