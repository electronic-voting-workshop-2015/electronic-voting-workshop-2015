class CreatePublications < ActiveRecord::Migration
  def change
      create_table :publications do |t|
          t.binary :content
      end
  end
end
