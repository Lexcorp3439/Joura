import re
import os

def metrics(log:str):
	all_events = re.findall(r"EVENTS COUNT = \d+", log)
	ebents_count = list(map(lambda x: int(x.replace("EVENTS COUNT = ", "")), all_events))
	tests_count = len(ebents_count)
	sum_event_count = sum(ebents_count)
	max_event_count = max(ebents_count)
	mean_event_count = float(sum_event_count) / max(tests_count, 1)
	print(f"Колчиество тестов: {tests_count}")
	print(f"Суммарное колчиество событий: {sum_event_count}")
	print(f"Максимальное колчиество событий: {max_event_count}")
	print(f"Среднее колчиество событий: {mean_event_count}")
	return tests_count, sum_event_count, max_event_count, mean_event_count

with open(f"{os.getcwd()}/Desktop/always_anal.txt") as infile:
		text = infile.read()
		tests_count_alw, sum_event_count_alw, max_event_count_alw, mean_event_count_alw = metrics(text)

with open(f"{os.getcwd()}/Desktop/alias_anal.txt") as infile:
		text = infile.read()
		tests_count_ali, sum_event_count_ali, max_event_count_ali, mean_event_count_ali = metrics(text)


with open(f"{os.getcwd()}/Desktop/always_true.txt") as infile:
		text = infile.read()
		tests_count_alt, sum_event_count_alt, max_event_count_alt, mean_event_count_alt = metrics(text)

print(f"Процент оптимизации: {(sum_event_count_alw - sum_event_count_ali)/sum_event_count_alw}")	
print(f"Процент оптимизации (true): {(sum_event_count_alt - sum_event_count_ali)/sum_event_count_alt}")	