import cv2
import mediapipe as mp
import pandas as pd

def create_dataset(directory, shape=224):
    hands = mp.solutions.hands.Hands(static_image_mode=False, max_num_hands=1, min_detection_confidence=0.5)
    video = cv2.VideoCapture(0)
    img_total = 0
    loop = True
    label = ""

    try:
        df = pd.read_csv(f"{directory}/labels.csv")
        img_total = len(df)
    except:
        df = pd.DataFrame(columns=['image', 'label'])

    while loop:
        label = input("Enter the letter: ")

        if label != "ESC":
            img_per_label = 0

            while img_per_label < 100:
                difference = 0
                ret, frame = video.read()
                height, width, _ = frame.shape
                if not ret:
                    continue
                results = hands.process(frame)

                if results.multi_hand_landmarks:
                    xmin, xmax, ymin, ymax = float('inf'), float('-inf'), float('inf'), float('-inf')
                    for i in range(21):
                        landmarks = results.multi_hand_landmarks[0].landmark
                        punto = landmarks[i]
                        x = punto.x
                        y = punto.y
                        xmin = min(x, xmin)
                        xmax = max(x, xmax)
                        ymin = min(y, ymin)
                        ymax = max(y, ymax)

                    xmin = int(xmin * width)
                    xmax = int(xmax * width)
                    ymin = int(ymin * height)
                    ymax = int(ymax * height)

                    if (xmax - xmin) <= (ymax - ymin):
                        difference = ymax - ymin
                    else:
                        difference = xmax - xmin

                    ymin_recorte = max(ymin - int(difference * 0.25), 0)
                    ymax_recorte = min(ymin + int(difference * 1.25), frame.shape[0])
                    xmin_recorte = max(xmin - int(difference * 0.25), 0)
                    xmax_recorte = min(xmin + int(difference * 1.25), frame.shape[1])

                    recorte = frame[ymin_recorte:ymax_recorte, xmin_recorte:xmax_recorte]

                    height, width, _ = recorte.shape
                    if recorte.size > 0 and height > shape and width > shape:
                        recorte = cv2.resize(recorte, (shape, shape))
                        df.loc[len(df)] = [f"{img_total}.jpg", label]
                        cv2.imwrite(f"{directory}/images/{img_total}.jpg", recorte)
                        img_per_label += 1
                        img_total += 1
                        cv2.rectangle(frame, (xmin_recorte, ymin_recorte), (xmax_recorte,ymax_recorte ), (0, 255, 0), 2)
                    else:
                        cv2.rectangle(frame, (xmin_recorte, ymin_recorte), (xmax_recorte,ymax_recorte ), (0, 0, 255), 2)

                cv2.imshow('Create dataset', frame)

                if cv2.waitKey(1) & 0xFF == 27:
                    break

            df.to_csv(f"{directory}/labels.csv", index=False)
        else:
            loop = False

    video.release()
    cv2.destroyAllWindows()

#create_dataset("data")
